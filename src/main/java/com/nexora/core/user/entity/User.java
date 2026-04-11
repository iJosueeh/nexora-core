package com.nexora.core.user.entity;

import com.nexora.core.common.entity.AuditableBaseEntity;


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
}
