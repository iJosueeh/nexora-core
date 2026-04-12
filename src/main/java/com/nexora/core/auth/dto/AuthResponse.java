package com.nexora.core.auth.dto;

import java.util.UUID;

import com.nexora.core.user.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UUID userId;
    private String email;
    private Role role;

    private String username;
    private String fullName;
}
