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

    // Get all bookings (Admin only)
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long machineId
    ) {
        return ResponseEntity.ok(bookingService.getBookings(userId, machineId));
    }

    // Endpoint: GET /api/bookings/my
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    // Endpoint: DELETE /api/bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok("Booking cancelled successfully.");
        } catch (RuntimeException e) {
            // Return 403 or 400 depending on the error, for now 400 is fine for simplicity
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}