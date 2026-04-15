package com.nexora.core.graphql.dto;

import java.util.UUID;

public record FeedAuthorView(
        UUID usuarioId,
        String username,
        String fullName,
        String avatarUrl) {
}
