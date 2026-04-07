package com.template.jwtstarter.user.entity;

import com.template.jwtstarter.common.entity.BaseEntity;
import com.template.jwtstarter.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private Role role;
}
