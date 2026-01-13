// java
package com.laundry.app.controller;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Booking;
import com.laundry.app.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing booking endpoints for creating, listing and cancelling bookings.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Construct a BookingController with the given BookingService.
     *
     * @param bookingService service used to manage bookings
     */
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking based on the provided request.
     *
     * @param request booking request payload containing start/end times and machine type
     * @return ResponseEntity containing the created Booking and HTTP 200 OK
     */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        Booking newBooking = bookingService.createBooking(request);
        return ResponseEntity.ok(newBooking);
    }

    /**
     * Retrieve all bookings, optionally filtered by userId and/or machineId.
     *
     * @param userId optional user id to filter bookings
     * @param machineId optional machine id to filter bookings
     * @return ResponseEntity with list of matching bookings and HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long machineId
    ) {
        return ResponseEntity.ok(bookingService.getBookings(userId, machineId));
    }

    /**
     * Retrieve active bookings for the currently authenticated user.
     *
     * @return ResponseEntity with list of active bookings for current user and HTTP 200 OK
     */
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyActiveBookings());
    }

    /**
     * Cancel a booking by id.
     *
     * @param id id of the booking to cancel
     * @return ResponseEntity with success message or bad request with error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok("Booking cancelled successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
