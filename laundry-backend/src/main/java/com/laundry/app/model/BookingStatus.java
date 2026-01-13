package com.laundry.app.model;

/**
 * BookingStatus represents the lifecycle state of a booking.
 */
public enum BookingStatus {
    /** Waiting for confirmation or payment. */
    PENDING,

    /** Booking confirmed and scheduled. */
    CONFIRMED,

    /** Booking completed (past). */
    COMPLETED,

    /** Booking cancelled and no longer active. */
    CANCELLED
}
