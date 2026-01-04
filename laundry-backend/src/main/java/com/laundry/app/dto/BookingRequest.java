package com.laundry.app.dto;

import com.laundry.app.model.MachineType;
import java.time.LocalDateTime;

public class BookingRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // New field: User chooses WASHER or DRYER
    private MachineType machineType;

    public BookingRequest() {}

    // Getters & Setters
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public MachineType getMachineType() { return machineType; }
    public void setMachineType(MachineType machineType) { this.machineType = machineType; }
}