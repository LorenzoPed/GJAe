package com.laundry.app.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus; // Assicurati di avere questo Enum, altrimenti usa String "CONFIRMED"
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import com.laundry.app.model.MachineType;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, MachineRepository machineRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new booking.
     * LOGIC CHANGED: User selects TIME, System selects MACHINE.
     */
    public Booking createBooking(BookingRequest request) {

        // 0. BASIC VALIDATION
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in the past");
        }

        // 1. Get the username from the Security Context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the full User entity
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // --- 3. AUTO-ASSIGNMENT LOGIC (UPDATED) ---
        // Change: Instead of fetching ALL machines, we fetch only the requested type (WASHER/DRYER)
        List<Machine> candidateMachines = machineRepository.findByType(request.getMachineType());

        Machine selectedMachine = null;

        for (Machine machine : candidateMachines) {
            // Check 1: Is the machine enabled?
            if (machine.isEnabled()) {
                // Check 2: Is the machine free? (no overlap)
                boolean isOccupied = bookingRepository.existsOverlap(
                        machine.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

                if (!isOccupied) {
                    // Found a free machine of the correct type
                    selectedMachine = machine;
                    break;
                }
            }
        }

        // 4. If selectedMachine is still null, it means no machines of that type are free
        if (selectedMachine == null) {
            throw new IllegalStateException("No " + request.getMachineType() + " available for the selected time slot.");
        }

        // 5. Create the Booking object
        Booking booking = new Booking();
        booking.setMachine(selectedMachine);
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.CONFIRMED);

        // 6. Save to DB
        return bookingRepository.save(booking);
    }

    // Method to get all bookings (Standard findAll)
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Method to get all the bookings of currently logged-in user
    @Transactional(readOnly = true)
    public List<Booking> getMyBookings() {
        // 1. Get username from token (Security Context)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Ask DB for bookings linked to this username
        return bookingRepository.findByUser_Username(username);
    }

    // Method to cancel a booking (Security checked)
    public void cancelBooking(Long bookingId) {
        // 1. Find the booking or throw error
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // 2. Get current logged-in user details
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // We need to check if the user is a MANAGER to allow override
        boolean isManager = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        // 3. SECURITY CHECK:
        // Allow delete ONLY if the booking belongs to the user OR if user is a MANAGER
        if (!booking.getUser().getUsername().equals(currentUsername) && !isManager) {
            throw new RuntimeException("You are not authorized to cancel this booking.");
        }

        // 4. Delete the booking
        bookingRepository.delete(booking);
    }

    // Ritorna TRUE se c'è almeno una macchina libera di quel tipo
    public boolean isSlotAvailable(MachineType type, LocalDateTime start, LocalDateTime end) {
        long totalMachines = machineRepository.countByType(type);
        long activeBookings = bookingRepository.countConflictingBookings(type, start, end);

        // Se le prenotazioni sono meno delle macchine totali, c'è posto!
        return activeBookings < totalMachines;
    }

    // Advanced search method for Manager dashboard (Filter by User or Machine)
    public List<Booking> getBookings(Long userId, Long machineId) {
        if (userId != null) {
            return bookingRepository.findByUserId(userId);
        } else if (machineId != null) {
            return bookingRepository.findByMachineId(machineId);
        } else {
            return bookingRepository.findAll();
        }
    }

}