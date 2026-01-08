package com.laundry.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED

    // RELATIONSHIP: Many bookings can belong to One machine
    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = true)
    private Machine machine;

    // --- NEW CHANGE: Link to User ---
    // Many bookings can belong to One user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Booking() {}

    // Updated Constructor
    public Booking(LocalDateTime startTime, LocalDateTime endTime, BookingStatus status, Machine machine, User user) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.machine = machine;
        this.user = user;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}