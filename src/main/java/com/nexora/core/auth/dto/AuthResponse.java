package com.nexora.core.auth.dto;

import java.util.List;
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
    private String bio;
    private String career;
    private String avatarUrl;
    private String bannerUrl;
    private Integer followersCount;
    private Integer followingCount;
    private List<String> academicInterests;
    private Boolean profileComplete;
}
