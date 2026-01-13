package com.laundry.app.model;

/**
 * Available roles in the system.
 */
public enum Role {
    /** Regular application user with limited privileges. */
    USER,

    /** Manager with elevated privileges (can manage machines, view all bookings, etc.). */
    MANAGER
}
