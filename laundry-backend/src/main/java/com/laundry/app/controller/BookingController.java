package com.laundry.app.controller;

import com.laundry.app.dto.BookingMapper;
import com.laundry.app.dto.BookingRequest;
import com.laundry.app.dto.BookingResponse;
import com.laundry.app.model.Booking;
import com.laundry.app.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    // Constructor injection (Manual constructor to avoid Lombok issues)
    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    // Endpoint to create a new booking
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        // Call the service to create the booking
        Booking booking = bookingService.createBooking(
                request.getUserId(),
                request.getMachineId(),
                request.getStartTime(),
                request.getEndTime()
        );

        // Convert the Booking entity to BookingResponse DTO
        BookingResponse response = bookingMapper.toResponse(booking);

        return ResponseEntity.ok(response);
    }
}