package com.nexora.core.graphql.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FeedPostView(
    UUID id,
    String titulo,
    String contenido,
    Boolean isOfficial,
    OffsetDateTime createdAt,
    int commentsCount,
    int likesCount,
    boolean isLiked,
    FeedAuthorView autor,
    List<String> tags,
    String location,
    String imageUrl
) {}

