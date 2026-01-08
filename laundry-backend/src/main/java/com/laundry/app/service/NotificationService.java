package com.laundry.app.service;

import com.laundry.app.model.Notification;
import com.laundry.app.model.User;
import com.laundry.app.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired private NotificationRepository notificationRepository;

    public void sendNotification(User user, String message) {
        Notification n = new Notification(user, message);
        notificationRepository.save(n);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
