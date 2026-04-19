package com.nexora.core.graphql.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentThreadView {

    private final UUID id;
    private final UUID postId;
    private final UUID parentId;
    private final UUID autorId;
    private final String contenido;
    private final OffsetDateTime createdAt;
    private final List<CommentThreadView> respuestas = new ArrayList<>();

    public CommentThreadView(UUID id, UUID postId, UUID parentId, UUID autorId, String contenido, OffsetDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.parentId = parentId;
        this.autorId = autorId;
        this.contenido = contenido;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public UUID getAutorId() {
        return autorId;
    }

    public String getContenido() {
        return contenido;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<CommentThreadView> getRespuestas() {
        return respuestas;
    }
}
