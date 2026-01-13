package com.laundry.app.model;

import jakarta.persistence.*;

/**
 * Represents a machine (washer or dryer) in the laundry application.
 */
@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private MachineType type;
    private boolean enabled;

    /**
     * Default constructor required by JPA.
     */
    public Machine() {}

    /**
     * Create a new Machine.
     *
     * @param name machine name
     * @param type machine type (WASHER/DRYER)
     * @param enabled whether the machine is enabled
     */
    public Machine(String name, MachineType type, boolean enabled) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
    }

    // Getters & Setters

    /**
     * Returns the machine id.
     * @return id
     */
    public Long getId() { return id; }
    /**
     * Set the machine id.
     * @param id id to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns the machine name.
     * @return name
     */
    public String getName() { return name; }
    /**
     * Set the machine name.
     * @param name name to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the machine type.
     * @return machine type
     */
    public MachineType getType() { return type; }
    /**
     * Set the machine type.
     * @param type type to set
     */
    public void setType(MachineType type) { this.type = type; }

    /**
     * Returns whether the machine is enabled.
     * @return true if enabled
     */
    public boolean isEnabled() { return enabled; }
    /**
     * Enable or disable the machine.
     * @param enabled new enabled state
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}