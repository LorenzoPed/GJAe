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

@Component
@ViewScoped
public class NotificationView implements Serializable {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository; // <--- Ci serve questo per trovare l'ID

    private List<Notification> unreadNotifications;

    @PostConstruct
    public void init() {
        loadNotifications();
    }

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

    public void markRead(Notification n) {
        if (n != null) {
            notificationService.markAsRead(n.getId());
            loadNotifications(); // Ricarica la lista per aggiornare il numero
        }
    }

    public List<Notification> getUnreadNotifications() {
        return unreadNotifications;
    }

    public int getCount() {
        return unreadNotifications == null ? 0 : unreadNotifications.size();
    }
}