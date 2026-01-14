package com.laundry.app.laundry_backend.service;

import com.laundry.app.model.Notification;
import com.laundry.app.repository.NotificationRepository;
import com.laundry.app.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationService verifying persistence and marking as read.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;

    @Test
    void sendNotification_savesNotification() {
        // user object is not used directly by repository in the service (only wrapped in Notification)
        notificationService.sendNotification(null, "hello");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_delegatesToRepository() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of());
        List<Notification> res = notificationService.getUnreadNotifications(5L);
        assertNotNull(res);
        verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(5L);
    }

    @Test
    void markAsRead_updatesAndSaves() {
        Notification mocked = mock(Notification.class);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(mocked));

        notificationService.markAsRead(10L);

        verify(mocked).setRead(true);
        verify(notificationRepository).save(mocked);
    }
}
