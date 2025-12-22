package com.laundry.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Meglio Long autogenerato

    private String name; // Es: "Lavatrice 1", "Moka Machine"
    private String type; // Es: "WASHER", "DRYER"
    private boolean enabled; // Meglio chiamarlo 'enabled' (funzionante/rotta)

    public Machine() {}

    public Machine(String name, String type, boolean enabled) {
        this.name = name;
        this.type = type;
        this.enabled = enabled;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}