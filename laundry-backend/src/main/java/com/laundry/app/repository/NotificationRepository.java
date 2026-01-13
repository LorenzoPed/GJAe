package com.laundry.app.repository;

import com.laundry.app.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Notification entities.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find unread notifications for a user ordered by creation time descending.
     * @param userId recipient user id
     * @return list of notifications
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}