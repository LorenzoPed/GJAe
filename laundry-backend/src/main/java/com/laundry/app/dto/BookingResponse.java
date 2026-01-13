// java
package com.laundry.app.dto;

import java.time.LocalDateTime;

/**
 * DTO returned to clients describing a booking (id, user, machine name and times).
 */
public class BookingResponse {
    private Long id;
    private Long userId;
    private String machineName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * Default empty constructor for serialization frameworks.
     */
    public BookingResponse() {}

    /**
     * Create a BookingResponse with all fields.
     *
     * @param id booking id
     * @param userId user id who made the booking
     * @param machineName human\-readable machine name
     * @param startTime booking start time
     * @param endTime booking end time
     */
    public BookingResponse(Long id, Long userId, String machineName, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.userId = userId;
        this.machineName = machineName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Get booking id.
     *
     * @return booking id
     */
    public Long getId() { return id; }

    /**
     * Set booking id.
     *
     * @param id id to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Get user id for this booking.
     *
     * @return user id
     */
    public Long getUserId() { return userId; }

    /**
     * Set the user id for this booking.
     *
     * @param userId user id to set
     */
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * Get the machine name used in the booking.
     *
     * @return machine name
     */
    public String getMachineName() { return machineName; }

    /**
     * Set the machine name for this booking response.
     *
     * @param machineName machine name to set
     */
    public void setMachineName(String machineName) { this.machineName = machineName; }

    /**
     * Get start time of the booking.
     *
     * @return start time
     */
    public LocalDateTime getStartTime() { return startTime; }

    /**
     * Set start time of the booking.
     *
     * @param startTime start time to set
     */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /**
     * Get end time of the booking.
     *
     * @return end time
     */
    public LocalDateTime getEndTime() { return endTime; }

    /**
     * Set end time of the booking.
     *
     * @param endTime end time to set
     */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
