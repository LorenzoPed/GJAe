package com.laundry.app.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
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

    public BookingService(
        BookingRepository bookingRepository,
        MachineRepository machineRepository,
        UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
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

            if (!isOccupied) {
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

    public boolean isSlotAvailable(MachineType type, LocalDateTime start, LocalDateTime end) {
        long enabledMachines = machineRepository.countByTypeAndEnabledTrue(type);
        if (enabledMachines <= 0) {
            return false;
        }

        long activeBookings = bookingRepository.countConflictingBookings(
            type,
            start,
            end,
            BookingStatus.CANCELLED
        );

        return activeBookings < enabledMachines;
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
}
