package com.laundry.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime; // Data e ora inizio lavaggio
    private LocalDateTime endTime;   // Data e ora fine previsti

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne // Many bookings-> One User
    @JoinColumn(name = "user_id", nullable = false) // Creates column user_id in the DB
    private User user;

    @ManyToOne // Many bookings -> One Machine
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }

    // Empty constructor
    public Booking() {}

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}