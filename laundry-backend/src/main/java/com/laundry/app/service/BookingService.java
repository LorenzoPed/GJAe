package com.laundry.app.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.Machine;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.User;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    // Create a new booking linked to the currently logged-in user
    public Booking createBooking(BookingRequest request) {

        // 0. BASIC VALIDATION: Check if dates make sense
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (request.getStartTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in the past");
        }

        // 1. Get the username from the Security Context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the full User entity from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Find the Machine
        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new RuntimeException("Machine not found"));

        // 4. CRITICAL CHECK: Ensure the machine is not already booked
        boolean isOccupied = bookingRepository.existsOverlap(
                machine.getId(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (isOccupied) {
            throw new IllegalStateException("The machine is already booked for this time slot.");
        }

        // 5. Create the Booking object
        Booking booking = new Booking();
        booking.setMachine(machine);
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.CONFIRMED);

        // 6. Save to DB
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}