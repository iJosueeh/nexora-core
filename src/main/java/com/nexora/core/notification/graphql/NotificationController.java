package com.nexora.core.notification.graphql;

import com.nexora.core.notification.graphql.dto.NotificationView;
import com.nexora.core.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationService notificationService;

    @QueryMapping
    public List<NotificationView> notificationHistory(@Argument Integer limit, @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, offset);
        return notificationQueryService.getNotificationHistory(safeLimit, safeOffset);
    }

    @QueryMapping
    public int unreadNotificationsCount() {
        return (int) notificationQueryService.getUnreadCount();
    }

    @MutationMapping
    public boolean markNotificationAsRead(@Argument UUID notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    @MutationMapping
    public boolean markAllNotificationsAsRead() {
        return notificationService.markAllAsRead();
    }
}
