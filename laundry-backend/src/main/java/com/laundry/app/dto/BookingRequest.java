package com.laundry.app.dto;

import java.time.LocalDateTime;

public class BookingRequest {
    private Long machineId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}