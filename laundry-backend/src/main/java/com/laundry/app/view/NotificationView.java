package com.laundry.app.view;

import com.laundry.app.model.Notification;
import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository; // <--- Importante
import com.laundry.app.service.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * JSF view bean for user notifications: load unread notifications and mark them as read.
 */
@Component
@ViewScoped
public class NotificationView implements Serializable {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository; // <--- Ci serve questo per trovare l'ID

    private List<Notification> unreadNotifications;

    /**
     * Initialize and load current user's unread notifications.
     */
    @PostConstruct
    public void init() {
        loadNotifications();
    }

    /**
     * Load unread notifications for the currently authenticated user.
     */
    public void loadNotifications() {
        // 1. Prendi lo username di chi è loggato ora
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Cerca l'utente nel DB usando lo username
        Optional<User> userOpt = userRepository.findByUsername(username);

        // 3. Se l'utente esiste, prendi il suo ID e carica le notifiche
        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getId(); // <--- Ora userId è inizializzato!
            this.unreadNotifications = notificationService.getUnreadNotifications(userId);
        }
    }

    /**
     * Mark a notification as read and refresh the list.
     *
     * @param n notification to mark as read
     */
    public void markRead(Notification n) {
        if (n != null) {
            notificationService.markAsRead(n.getId());
            loadNotifications(); // Ricarica la lista per aggiornare il numero
        }
    }

    /**
     * Return unread notifications for display.
     *
     * @return list of unread notifications
     */
    public List<Notification> getUnreadNotifications() {
        return unreadNotifications;
    }

    /**
     * Return the count of unread notifications.
     *
     * @return number of unread notifications
     */
    public int getCount() {
        return unreadNotifications == null ? 0 : unreadNotifications.size();
    }
}