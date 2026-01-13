package com.laundry.app.service;

import com.laundry.app.model.Notification;
import com.laundry.app.model.User;
import com.laundry.app.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for creating and querying user notifications.
 */
@Service
public class NotificationService {
    @Autowired private NotificationRepository notificationRepository;

    /**
     * Persist and send a notification to a user (stored in DB).
     *
     * @param user recipient user
     * @param message message text
     */
    public void sendNotification(User user, String message) {
        Notification n = new Notification(user, message);
        notificationRepository.save(n);
    }

    /**
     * Retrieve unread notifications for a user ordered by creation time descending.
     *
     * @param userId recipient user id
     * @return list of unread notifications
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId id of the notification to mark
     */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
