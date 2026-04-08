package com.nexora.core.user.dto;

import java.util.UUID;

import com.nexora.core.user.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private Boolean isActive;
    private Role role;
}
