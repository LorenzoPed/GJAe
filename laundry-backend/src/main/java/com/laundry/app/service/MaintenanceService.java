package com.laundry.app.service;

import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.Maintenance;
import com.laundry.app.model.MaintenanceStatus;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MaintenanceRepository;
import com.laundry.app.repository.MachineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService {

    public static class MaintenanceResult {
        private final int impactedBookings;
        private final int rescheduledBookings;
        private final int cancelledBookings;

        public MaintenanceResult(int impactedBookings, int rescheduledBookings, int cancelledBookings) {
            this.impactedBookings = impactedBookings;
            this.rescheduledBookings = rescheduledBookings;
            this.cancelledBookings = cancelledBookings;
        }

        public int getImpactedBookings() {
            return impactedBookings;
        }

        public int getRescheduledBookings() {
            return rescheduledBookings;
        }

        public int getCancelledBookings() {
            return cancelledBookings;
        }
    }

    private final MaintenanceRepository maintenanceRepository;
    private final MachineRepository machineRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService; // <--- 1. NUOVO

    public MaintenanceService(
            MaintenanceRepository maintenanceRepository,
            MachineRepository machineRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService // <--- 1. INIEZIONE
    ) {
        this.maintenanceRepository = maintenanceRepository;
        this.machineRepository = machineRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public MaintenanceResult scheduleMaintenance(Long machineId, LocalDateTime start, LocalDateTime end, String reason) {
        if (machineId == null) {
            throw new IllegalArgumentException("Machine is required.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now.minusMinutes(1))) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + machineId));

        boolean overlapsExisting = maintenanceRepository.existsOverlap(
                machineId,
                start,
                end,
                MaintenanceStatus.CANCELLED
        );
        if (overlapsExisting) {
            throw new IllegalArgumentException("This machine already has a maintenance scheduled during this period.");
        }

        List<Booking> impacted = bookingRepository.findOverlappingBookingsForMachine(
                machineId,
                start,
                end,
                BookingStatus.CANCELLED
        );

        // Qui chiamiamo il metodo helper che ora invia anche le notifiche
        MaintenanceResult result = rescheduleOrCancelBookings(machine, impacted);

        String normalizedReason = (reason == null || reason.trim().isEmpty()) ? null : reason.trim();
        Maintenance maintenance = new Maintenance(machine, start, end, normalizedReason);
        maintenanceRepository.save(maintenance);

        return result;
    }

    /**
     * Called when a machine is disabled indefinitely:
     * reschedule/cancel all future bookings on this machine.
     */
    @Transactional
    public MaintenanceResult handleMachineDisabled(Long machineId) {
        if (machineId == null) {
            throw new IllegalArgumentException("Machine is required.");
        }

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + machineId));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> impacted = bookingRepository.findFutureBookingsForMachine(
                machineId,
                now,
                BookingStatus.CANCELLED
        );

        return rescheduleOrCancelBookings(machine, impacted);
    }

    @Transactional(readOnly = true)
    public List<Maintenance> getUpcomingMaintenances(Long machineId) {
        if (machineId == null) {
            return List.of();
        }
        return maintenanceRepository.findUpcoming(machineId, LocalDateTime.now(), MaintenanceStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Long> getMachineIdsUnderMaintenanceNow() {
        return maintenanceRepository.findMachineIdsUnderMaintenanceAt(LocalDateTime.now(), MaintenanceStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public boolean isMachineUnderMaintenanceNow(Long machineId) {
        return maintenanceRepository.existsActiveAt(machineId, LocalDateTime.now(), MaintenanceStatus.CANCELLED);
    }

    @Transactional
    public void cancelMaintenance(Long maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance not found with id: " + maintenanceId));

        if (maintenance.getStatus() == MaintenanceStatus.CANCELLED) {
            return;
        }

        maintenance.setStatus(MaintenanceStatus.CANCELLED);
        maintenanceRepository.save(maintenance);
    }

    // --- MODIFICATO: Metodo Helper con Notifiche ---
    private MaintenanceResult rescheduleOrCancelBookings(Machine originalMachine, List<Booking> impacted) {
        int rescheduled = 0;
        int cancelled = 0;

        for (Booking booking : impacted) {
            Optional<Machine> alternative = findAlternativeMachineForBooking(originalMachine, booking);

            if (alternative.isPresent()) {
                // --- SUCCESSO: Spostiamo la prenotazione ---
                Machine newMachine = alternative.get();
                booking.setMachine(newMachine);
                bookingRepository.save(booking);
                rescheduled++;

                // NOTIFICA RISCHEDULAZIONE
                String msg = String.format(
                        "UPDATE: Your booking on %s has been moved to machine '%s' due to scheduled maintenance.",
                        booking.getStartTime().toLocalDate(),
                        newMachine.getName()
                );
                notificationService.sendNotification(booking.getUser(), msg);

            } else {
                // --- FALLIMENTO: Cancelliamo la prenotazione ---
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                cancelled++;

                // NOTIFICA CANCELLAZIONE
                String msg = String.format(
                        "ALERT: Your booking on %s has been CANCELLED due to scheduled maintenance on the machine. No alternatives were available.",
                        booking.getStartTime().toLocalDate()
                );
                notificationService.sendNotification(booking.getUser(), msg);
            }
        }

        return new MaintenanceResult(impacted.size(), rescheduled, cancelled);
    }

    private Optional<Machine> findAlternativeMachineForBooking(Machine originalMachine, Booking booking) {
        MachineType type = originalMachine.getType();
        List<Machine> candidates = machineRepository.findByTypeAndEnabledTrue(type);

        for (Machine candidate : candidates) {
            if (candidate.getId().equals(originalMachine.getId())) {
                continue;
            }

            boolean bookingOverlap = bookingRepository.existsOverlap(
                    candidate.getId(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    BookingStatus.CANCELLED
            );
            if (bookingOverlap) {
                continue;
            }

            boolean maintenanceOverlap = maintenanceRepository.existsOverlap(
                    candidate.getId(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    MaintenanceStatus.CANCELLED
            );
            if (maintenanceOverlap) {
                continue;
            }

            return Optional.of(candidate);
        }

        return Optional.empty();
    }
}