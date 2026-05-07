package com.nexora.core.graphql.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public record CommentThreadView(
    UUID id,
    UUID postId,
    UUID parentId,
    UUID autorId,
    String contenido,
    OffsetDateTime createdAt,
    List<CommentThreadView> respuestas
) {
    public CommentThreadView(UUID id, UUID postId, UUID parentId, UUID autorId, String contenido, OffsetDateTime createdAt) {
        this(id, postId, parentId, autorId, contenido, createdAt, new ArrayList<>());
    }
}
