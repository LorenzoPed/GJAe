package com.laundry.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Booking entity representing a reserved timeslot on a machine for a given user.
 * Contains start/end times, status and optional machine link.
 */
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

    /**
     * Default constructor required by JPA.
     */
    public Booking() {}

    /**
     * Create a new Booking instance.
     *
     * @param startTime the booking start time
     * @param endTime the booking end time
     * @param status the booking status
     * @param machine the associated machine (may be null)
     * @param user the user who created the booking
     */
    public Booking(LocalDateTime startTime, LocalDateTime endTime, BookingStatus status, Machine machine, User user) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.machine = machine;
        this.user = user;
    }

    // --- GETTERS & SETTERS ---

    /**
     * Returns the booking id.
     * @return id of the booking
     */
    public Long getId() { return id; }

    /**
     * Returns the booking start time.
     * @return start time
     */
    public LocalDateTime getStartTime() { return startTime; }
    /**
     * Set the booking start time.
     * @param startTime new start time
     */
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /**
     * Returns the booking end time.
     * @return end time
     */
    public LocalDateTime getEndTime() { return endTime; }
    /**
     * Set the booking end time.
     * @param endTime new end time
     */
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    /**
     * Returns the booking status.
     * @return booking status
     */
    public BookingStatus getStatus() { return status; }
    /**
     * Set the booking status.
     * @param status new status
     */
    public void setStatus(BookingStatus status) { this.status = status; }

    /**
     * Returns the associated machine.
     * @return machine or null
     */
    public Machine getMachine() { return machine; }
    /**
     * Set the associated machine.
     * @param machine machine to associate
     */
    public void setMachine(Machine machine) { this.machine = machine; }

    /**
     * Returns the user owning this booking.
     * @return booking user
     */
    public User getUser() { return user; }
    /**
     * Set the user owning this booking.
     * @param user user to set
     */
    public void setUser(User user) { this.user = user; }
}