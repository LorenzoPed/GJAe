package com.laundry.app.model;

import jakarta.persistence.*;

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

    public Machine() {}

    public Machine(String name, MachineType type, boolean enabled) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MachineType getType() { return type; }
    public void setType(MachineType type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}