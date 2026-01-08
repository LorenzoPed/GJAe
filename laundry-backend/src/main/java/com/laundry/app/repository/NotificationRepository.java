package com.laundry.app.repository;

import com.laundry.app.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Trova le notifiche non lette di un utente, ordinate dalla più recente
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}