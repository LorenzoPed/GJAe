package com.laundry.app.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.MaintenanceStatus;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.User;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MaintenanceRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    public static class DisableMachineResult {
        private final int impactedBookings;
        private final int rescheduledBookings;
        private final int cancelledBookings;

        public DisableMachineResult(int impactedBookings, int rescheduledBookings, int cancelledBookings) {
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

    private final BookingRepository bookingRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;
    private final MaintenanceRepository maintenanceRepository;

    public BookingService(
        BookingRepository bookingRepository,
        MachineRepository machineRepository,
        UserRepository userRepository,
        MaintenanceRepository maintenanceRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    public Booking createBooking(BookingRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null || request.getMachineType() == null) {
            throw new IllegalArgumentException("Start time, end time, and machine type are required.");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in the past.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        List<Machine> candidates = machineRepository.findByTypeAndEnabledTrue(request.getMachineType());

        Machine selectedMachine = null;
        for (Machine machine : candidates) {
            boolean isOccupied = bookingRepository.existsOverlap(
                machine.getId(),
                request.getStartTime(),
                request.getEndTime(),
                BookingStatus.CANCELLED
            );

            boolean isUnderMaintenance = maintenanceRepository.existsOverlap(
                machine.getId(),
                request.getStartTime(),
                request.getEndTime(),
                MaintenanceStatus.CANCELLED
            );

            if (!isOccupied && !isUnderMaintenance) {
                selectedMachine = machine;
                break;
            }
        }

        if (selectedMachine == null) {
            throw new IllegalStateException("No " + request.getMachineType() + " available for the selected time slot.");
        }

        Booking booking = new Booking();
        booking.setMachine(selectedMachine);
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }
    public List<Booking> getAllBookingsByUser(User user) {
        return bookingRepository.findByUserOrderByStartTimeDesc(user);
    }
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Booking> getActiveBookings() {
        return bookingRepository.findByStatusNot(BookingStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getMyActiveBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingRepository.findByUser_UsernameAndStatusNot(username, BookingStatus.CANCELLED);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isManager = SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!booking.getUser().getUsername().equals(currentUsername) && !isManager) {
            throw new RuntimeException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    /**
     * Cancella (soft delete) tutte le prenotazioni attive/future di un utente specifico.
     * Usato dal Manager per ripulire i dati di uno studente.
     */
    @Transactional
    public void cancelAllUserBookings(Long userId) {
        // 1. Trova tutte le prenotazioni di questo utente
        List<Booking> userBookings = bookingRepository.findByUserId(userId);

        // 2. Itera su tutte le prenotazioni
        for (Booking booking : userBookings) {
            // Se la prenotazione non è già cancellata, impostala come CANCELLED
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
            }
        }

        // 3. Salva le modifiche nel database
        bookingRepository.saveAll(userBookings);
    }

    public boolean isSlotAvailable(MachineType type, LocalDateTime start, LocalDateTime end) {
        long enabledMachines = machineRepository.countByTypeAndEnabledTrue(type);
        if (enabledMachines <= 0) {
            return false;
        }

        long machinesUnderMaintenance = maintenanceRepository.countMachinesWithOverlappingMaintenanceByType(
            type,
            start,
            end,
            MaintenanceStatus.CANCELLED
        );

        long availableMachines = enabledMachines - machinesUnderMaintenance;
        if (availableMachines <= 0) {
            return false;
        }

        long activeBookings = bookingRepository.countConflictingBookings(
            type,
            start,
            end,
            BookingStatus.CANCELLED
        );

        return activeBookings < availableMachines;
    }

    public List<Booking> getBookings(Long userId, Long machineId) {
        if (userId != null) {
            return bookingRepository.findByUserId(userId);
        }
        if (machineId != null) {
            return bookingRepository.findByMachineId(machineId);
        }
        return bookingRepository.findAll();
    }

    /**
     * Called when a manager disables a machine.
     * Reschedules future bookings to another enabled machine of the same type when possible.
     * Otherwise cancels them.
     */
    @Transactional
    public DisableMachineResult handleMachineDisabled(Long machineId) {
        if (machineId == null) {
            return new DisableMachineResult(0, 0, 0);
        }

        Machine disabledMachine = machineRepository.findById(machineId)
            .orElseThrow(() -> new RuntimeException("Machine not found with id: " + machineId));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> impacted = bookingRepository.findFutureBookingsForMachine(
            machineId,
            now,
            BookingStatus.CANCELLED
        );

        int rescheduled = 0;
        int cancelled = 0;

        for (Booking booking : impacted) {
            Optional<Machine> alternative = findAlternativeMachineForBooking(disabledMachine, booking);
            if (alternative.isPresent()) {
                booking.setMachine(alternative.get());
                bookingRepository.save(booking);
                rescheduled++;
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                cancelled++;
            }
        }

        return new DisableMachineResult(impacted.size(), rescheduled, cancelled);
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
