package com.laundry.app.dto;

/**
 * Simple status DTO describing a service and its state.
 *
 * @param name the service name
 * @param status current state of the service
 */
public record Status(String name, String status) {
    // Maintain compatibility: older code expected service()/state() accessors.
    public String service() { return name; }
    public String state() { return status; }
}
