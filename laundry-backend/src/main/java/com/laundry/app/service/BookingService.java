package com.laundry.app.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus; // Assicurati di avere questo Enum, altrimenti usa String "CONFIRMED"
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
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

        // 0. BASIC VALIDATION: Check if dates make sense
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in the past");
        }

        // 1. Get the username from the Security Context (Token)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the full User entity from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. AUTO-ASSIGNMENT LOGIC: Find the first available machine
        // We fetch all machines and check them one by one.
        List<Machine> allMachines = machineRepository.findAll();
        Machine selectedMachine = null;

        for (Machine machine : allMachines) {
            // Check 1: Is the machine working? (enabled)
            // Check 2: Is the machine free at this time? (no overlap)
            if (machine.isEnabled()) {
                boolean isOccupied = bookingRepository.existsOverlap(
                        machine.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

                if (!isOccupied) {
                    // We found a free machine! Select it and stop searching.
                    selectedMachine = machine;
                    break;
                }
            }
        }

        // 4. If selectedMachine is still null, it means everything is full
        if (selectedMachine == null) {
            throw new IllegalStateException("No machines available for the selected time slot.");
        }

        // 5. Create the Booking object with the System-Selected machine
        Booking booking = new Booking();
        booking.setMachine(selectedMachine);
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        // Use Enum if you have it, or string "CONFIRMED" depending on your model
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