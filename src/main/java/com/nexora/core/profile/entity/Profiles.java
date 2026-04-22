package com.nexora.core.profile.entity;


import com.nexora.core.common.entity.BaseEntity;
import com.nexora.core.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "perfiles")
public class Profiles extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="carrera_id", nullable = true)
    private Courses carrera;

    @Column(name="username",unique = true, nullable = false)
    private String username;

    @Column(name="full_name")
    private String fullName;

    @Column(name="bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name="avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name="banner_url", columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(name="followers_count")
    private int followersCount;

    @Column(name="following_count")
    private int followingCount;


}
