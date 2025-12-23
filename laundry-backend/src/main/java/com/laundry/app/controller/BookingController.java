package com.laundry.app.controller;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a booking (User is inferred from Token)
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        Booking newBooking = bookingService.createBooking(request);
        return ResponseEntity.ok(newBooking);
    }

    // Get all bookings (Admin only ideally, but public for now)
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
}