package com.laundry.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Meglio Long autogenerato

    private String name; // Es: "Lavatrice 1", "Moka Machine"
    @Enumerated(EnumType.STRING) // Salva "WASHER" o "DRYER" nel DB come testo
    private MachineType type;
    private boolean enabled; // Meglio chiamarlo 'enabled' (funzionante/rotta)

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