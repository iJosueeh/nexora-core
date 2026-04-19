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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@Setter
@Table(name = "usuarios")
public class User extends AuditableBaseEntity implements UserDetails {

    @Column(name="email",unique = true, nullable = false)
    private String email;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Profiles profile;

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getPassword() {
        return ""; // No local password
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
