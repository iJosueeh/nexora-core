package com.nexora.core.graphql.dto;

import java.util.UUID;

public record FeedPostView(
        UUID id,
        String titulo,
        String contenido,
        boolean isOfficial,
        FeedAuthorView autor) {
}
