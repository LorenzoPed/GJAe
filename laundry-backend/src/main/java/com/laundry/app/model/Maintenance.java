package com.laundry.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Maintenance entity representing scheduled machine maintenance windows.
 */
@Entity
@Table(name = "maintenances")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @ManyToOne(optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    /**
     * Default constructor required by JPA.
     */
    public Maintenance() {
    }

    /**
     * Create a new Maintenance record for a machine.
     *
     * @param machine  machine under maintenance
     * @param startTime maintenance start
     * @param endTime   maintenance end
     * @param reason    optional reason or description
     */
    public Maintenance(Machine machine, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        this.machine = machine;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = MaintenanceStatus.SCHEDULED;
    }

    /**
     * Returns the maintenance id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the maintenance id.
     *
     * @param id id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the maintenance start time.
     *
     * @return start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Set the maintenance start time.
     *
     * @param startTime new start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the maintenance end time.
     *
     * @return end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Set the maintenance end time.
     *
     * @param endTime new end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the reason for maintenance.
     *
     * @return reason text
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason for maintenance.
     *
     * @param reason reason text
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the maintenance status.
     *
     * @return status
     */
    public MaintenanceStatus getStatus() {
        return status;
    }

    /**
     * Set the maintenance status.
     *
     * @param status new status
     */
    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }

    /**
     * Returns the machine under maintenance.
     *
     * @return machine
     */
    public Machine getMachine() {
        return machine;
    }

    /**
     * Set the machine under maintenance.
     *
     * @param machine machine to set
     */
    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}
