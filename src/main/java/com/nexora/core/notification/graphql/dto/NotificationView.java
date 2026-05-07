package com.nexora.core.notification.graphql.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.content.entity.UniversityEvent; // Or a view if preferred

public record NotificationView(
    UUID id,
    String type,
    String content,
    Boolean isRead,
    OffsetDateTime createdAt,
    FeedAuthorView sender,
    FeedPostView post,
    Object event // Simplified for now, or use a specific view
) {}
