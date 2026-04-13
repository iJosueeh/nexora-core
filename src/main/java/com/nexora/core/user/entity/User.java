package com.nexora.core.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.nexora.core.common.entity.AuditableBaseEntity;
import com.nexora.core.content.entity.Comment;
import com.nexora.core.content.entity.Post;
import com.nexora.core.profile.entity.Profiles;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "usuarios")
public class User extends AuditableBaseEntity {

    @Column(name="email",unique = true, nullable = false)
    private String email;

    @Column(name="password",nullable = false,columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Profiles profile;

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
}
