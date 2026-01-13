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

/**
 * Service handling booking lifecycle: creation, queries, cancellations and reactions when machines are disabled.
 */
@Service
public class BookingService {

    /**
     * Result DTO returned when a machine is disabled: counts of impacted/rescheduled/cancelled bookings.
     */
    public static class DisableMachineResult {
        private final int impactedBookings;
        private final int rescheduledBookings;
        private final int cancelledBookings;

        /**
         * Create a result instance.
         *
         * @param impactedBookings number of bookings impacted
         * @param rescheduledBookings number of bookings rescheduled
         * @param cancelledBookings number of bookings cancelled
         */
        public DisableMachineResult(int impactedBookings, int rescheduledBookings, int cancelledBookings) {
            this.impactedBookings = impactedBookings;
            this.rescheduledBookings = rescheduledBookings;
            this.cancelledBookings = cancelledBookings;
        }

        /**
         * Number of impacted bookings (future ones on the disabled machine).
         *
         * @return number of impacted bookings
         */
        public int getImpactedBookings() {
            return impactedBookings;
        }

        /**
         * Number of bookings successfully rescheduled to other machines.
         *
         * @return number of rescheduled bookings
         */
        public int getRescheduledBookings() {
            return rescheduledBookings;
        }

        /**
         * Number of bookings cancelled because no alternative was available.
         *
         * @return number of cancelled bookings
         */
        public int getCancelledBookings() {
            return cancelledBookings;
        }
    }

    private final BookingRepository bookingRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final NotificationService notificationService;

    /**
     * Create a BookingService with required repositories and notification service.
     *
     * @param bookingRepository repository for bookings
     * @param machineRepository repository for machines
     * @param userRepository repository for users
     * @param maintenanceRepository repository for maintenances
     * @param notificationService service to send user notifications
     */
    public BookingService(
            BookingRepository bookingRepository,
            MachineRepository machineRepository,
            UserRepository userRepository,
            MaintenanceRepository maintenanceRepository,
            NotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create a booking for the authenticated user if a machine of the requested type is available.
     *
     * @param request booking request containing times and machine type
     * @return the saved Booking
     */
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

    /**
     * Return all bookings belonging to the given user ordered descending by start time.
     *
     * @param user user entity
     * @return list of bookings
     */
    public List<Booking> getAllBookingsByUser(User user) {
        return bookingRepository.findByUserOrderByStartTimeDesc(user);
    }

    /**
     * Return all bookings in the system.
     *
     * @return all bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Return all bookings that are not cancelled.
     *
     * @return active bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getActiveBookings() {
        return bookingRepository.findByStatusNot(BookingStatus.CANCELLED);
    }

    /**
     * Return the current authenticated user's active bookings.
     *
     * @return list of bookings for current user
     */
    @Transactional(readOnly = true)
    public List<Booking> getMyActiveBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookingRepository.findByUser_UsernameAndStatusNot(username, BookingStatus.CANCELLED);
    }

    /**
     * Cancel a booking if the current user is owner or has manager role.
     *
     * @param bookingId id of the booking to cancel
     */
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
     * Delete (soft delete) all the active/future reservations of a given user.
     * Used by Manager to make bulk cancellation of a given user.
     * Sends a notification for each future booking cancelled.
     *
     * @param userId id of the user whose bookings will be cancelled
     */
    @Transactional
    public void cancelAllUserBookings(Long userId) {
        List<Booking> userBookings = bookingRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : userBookings) {

            if (booking.getStatus() != BookingStatus.CANCELLED) {

                if (booking.getEndTime().isAfter(now)) {
                    String msg = String.format(
                            "ADMIN NOTICE: Your booking on %s at %s has been cancelled by an administrator.",
                            booking.getStartTime().toLocalDate(),
                            booking.getStartTime().toLocalTime()
                    );
                    notificationService.sendNotification(booking.getUser(), msg);
                }

                booking.setStatus(BookingStatus.CANCELLED);
            }
        }
        bookingRepository.saveAll(userBookings);
    }

    /**
     * Check whether there is capacity for a booking of the given machine type in the interval.
     *
     * @param type machine type
     * @param start interval start
     * @param end interval end
     * @return true if slot is available
     */
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

    /**
     * Retrieve bookings optionally filtered by userId or machineId.
     *
     * @param userId optional user id
     * @param machineId optional machine id
     * @return list of bookings matching filters
     */
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
     * When a machine is disabled or deleted, attempt to reschedule its future bookings to other enabled machines.
     * If no alternative is found the booking is cancelled. Notifications are sent to affected users.
     *
     * @param machineId id of the disabled machine
     * @return result containing counts of impacted/rescheduled/cancelled bookings
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
                //SUCCESS: RESCHEDULED
                Machine newMachine = alternative.get();
                booking.setMachine(newMachine);
                bookingRepository.save(booking);
                rescheduled++;

                String msg = String.format(
                        "UPDATE: Your booking on %s has been moved to machine '%s' because the original one is unavailable.",
                        booking.getStartTime().toLocalDate(),
                        newMachine.getName()
                );
                notificationService.sendNotification(booking.getUser(), msg);

            } else {
                // --- FAILURE: CANCELED ---
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                cancelled++;

                String msg = String.format(
                        "ALERT: Your booking on %s has been CANCELLED. The machine is out of order/removed and no other machines are available at that time.",
                        booking.getStartTime().toLocalDate()
                );
                notificationService.sendNotification(booking.getUser(), msg);
            }
        }

        return new DisableMachineResult(impacted.size(), rescheduled, cancelled);
    }

    /**
     * Find an alternative enabled machine suitable for the given booking (no booking/maintenance overlap).
     *
     * @param originalMachine the machine being replaced
     * @param booking booking to relocate
     * @return optional alternative machine
     */
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

