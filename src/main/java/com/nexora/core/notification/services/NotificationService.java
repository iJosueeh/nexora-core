package com.nexora.core.notification.services;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.notification.entity.Notification;
import com.nexora.core.notification.repository.NotificationRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<Notification> getNotificationHistory(int limit, int offset) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    @Transactional
    public boolean markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        // Verificar que la notificación pertenece al usuario actual
        if (!notification.getUser().getId().equals(securityService.getCurrentUserId())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return true;
    }

    @Transactional
    public boolean markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsRead(currentUser);
        return true;
    }

    private User getCurrentUser() {
        UUID userId = securityService.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
