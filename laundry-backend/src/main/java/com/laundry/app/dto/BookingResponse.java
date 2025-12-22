package com.laundry.app.dto;

import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private Long userId;        // Restituiamo solo l'ID o il nome, non tutto l'oggetto User
    private String machineName; // Comodo per il frontend avere subito il nome
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Empty constructor
    public BookingResponse() {}

    public BookingResponse(Long id, Long userId, String machineName, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.userId = userId;
        this.machineName = machineName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}