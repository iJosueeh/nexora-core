package com.nexora.core.notification.services;

import com.nexora.core.notification.entity.Notification;
import com.nexora.core.notification.repository.NotificationRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@nexora.com");
    }

    @Test
    void getUnreadCountShouldReturnCount() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByUserAndIsReadFalse(testUser)).thenReturn(5L);

        long count = notificationService.getUnreadCount();

        assertEquals(5L, count);
        verify(notificationRepository).countByUserAndIsReadFalse(testUser);
    }

    @Test
    void markAsReadShouldSuccessWhenOwner() {
        UUID notifId = UUID.randomUUID();
        Notification notif = new Notification();
        notif.setId(notifId);
        notif.setUser(testUser);
        notif.setIsRead(false);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(securityService.getCurrentUserId()).thenReturn(userId);

        boolean result = notificationService.markAsRead(notifId);

        assertTrue(result);
        assertTrue(notif.getIsRead());
        verify(notificationRepository).save(notif);
    }

    @Test
    void markAsReadShouldThrowExceptionWhenNotOwner() {
        UUID notifId = UUID.randomUUID();
        Notification notif = new Notification();
        notif.setId(notifId);
        
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        notif.setUser(otherUser);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(securityService.getCurrentUserId()).thenReturn(userId);

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(notifId));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadShouldInvokeRepository() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = notificationService.markAllAsRead();

        assertTrue(result);
        verify(notificationRepository).markAllAsRead(testUser);
    }
}
