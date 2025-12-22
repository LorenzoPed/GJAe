package com.laundry.app.service;

import com.laundry.app.model.Booking;
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MachineRepository machineRepository;

    // COSTRUTTORE MANUALE (Sostituisce @RequiredArgsConstructor)
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          MachineRepository machineRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.machineRepository = machineRepository;
    }

    public Booking createBooking(Long userId, Long machineId, LocalDateTime start, LocalDateTime end) {

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con id: " + userId));

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Macchina non trovata con id: " + machineId));

        boolean isOccupied = bookingRepository.existsOverlap(machineId, start, end);
        if (isOccupied) {
            throw new RuntimeException("La macchina è già prenotata in questo intervallo di tempo.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setMachine(machine);
        booking.setStartTime(start);
        booking.setEndTime(end);

        return bookingRepository.save(booking);
    }
}