package com.laundry.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification entity representing a message sent to a user.
 */
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

    /**
     * Default constructor for JPA.
     */
    public Notification() {}

    /**
     * Create a notification for a user.
     * @param user the recipient user
     * @param message the notification message
     */
    public Notification(User user, String message) {
        this.user = user;
        this.message = message;
    }

    /**
     * Returns the notification id.
     * @return id
     */
    public Long getId() { return id; }

    /**
     * Returns the recipient user.
     * @return user
     */
    public User getUser() { return user; }

    /**
     * Set the recipient user.
     * @param user the user to set
     */
    public void setUser(User user) { this.user = user; }

    /**
     * Return the message text.
     * @return message
     */
    public String getMessage() { return message; }

    /**
     * Set the message text.
     * @param message message to set
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Whether the notification has been read.
     * @return true if read
     */
    public boolean isRead() { return isRead; }

    /**
     * Mark the notification as read/unread.
     * @param read new read state
     */
    public void setRead(boolean read) { isRead = read; }

    /**
     * Returns the creation timestamp.
     * @return creation time
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
