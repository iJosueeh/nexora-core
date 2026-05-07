package com.nexora.core.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "post_likes")
@IdClass(PostLikeId.class)
public class PostLike {

    @Id
    @Column(name = "post_id")
    private UUID postId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
