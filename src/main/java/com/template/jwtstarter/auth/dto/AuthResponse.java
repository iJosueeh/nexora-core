package com.template.jwtstarter.auth.dto;

import com.template.jwtstarter.user.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String email;
    private Role role;
}
