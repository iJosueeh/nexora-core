package com.nexora.core.profile.entity;


import com.nexora.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "perfiles")
public class Profiles extends BaseEntity {

    @Column(name="usuario_id",unique = true, nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="carrera_id", nullable = true)
    private Courses carrera;

    @Column(name="username",unique = true, nullable = false)
    private String username;

    @Column(name="full_name",unique = true, nullable = false)
    private String fullName;

    @Column(name="bio",unique = true, columnDefinition = "TEXT")
    private String bio;

    @Column(name="avatar_url",unique = true, columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name="banner_url",unique = true, columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(name="followers_count",unique = true)
    private int followersCount;


}
