package com.laundry.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Chi riceve il messaggio

    @Column(nullable = false)
    private String message;

    private boolean isRead = false; // Per sapere se l'ha già letta

    private LocalDateTime createdAt = LocalDateTime.now();

    // Costruttori, Getters e Setters
    public Notification() {}
    public Notification(User user, String message) {
        this.user = user;
        this.message = message;
    }

    // ... genera getter e setter standard ...
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
