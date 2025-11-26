package com.laundry.app.model;

public class Machine {
    private String id;
    private String type;      // e.g. "washer", "dryer"
    private boolean available;

    public Machine() {}

    public Machine(String id, String type, boolean available) {
        this.id = id;
        this.type = type;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
