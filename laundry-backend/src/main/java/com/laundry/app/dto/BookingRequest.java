// java
package com.laundry.app.dto;

import com.laundry.app.model.MachineType;
import java.time.LocalDateTime;

/**
 * DTO representing a booking request payload (start/end times and desired machine type).
 */
public class BookingRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // New field: User chooses WASHER or DRYER
    private MachineType machineType;

    /**
     * Default constructor for BookingRequest.
     */
    public BookingRequest() {}

    /**
     * Get the booking start time.
     *
     * @return start time as LocalDateTime
     */
    public LocalDateTime getStartTime() { return startTime; }

    /**
     * Set the booking start time.
     *
     * @param startTime start time to set
     */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /**
     * Get the booking end time.
     *
     * @return end time as LocalDateTime
     */
    public LocalDateTime getEndTime() { return endTime; }

    /**
     * Set the booking end time.
     *
     * @param endTime end time to set
     */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /**
     * Get the requested machine type for the booking.
     *
     * @return machine type enum value
     */
    public MachineType getMachineType() { return machineType; }

    /**
     * Set the requested machine type for the booking.
     *
     * @param machineType the machine type to set
     */
    public void setMachineType(MachineType machineType) { this.machineType = machineType; }
}
