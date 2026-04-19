package com.nexora.core.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.user.dto.UserResponse;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.info("Encontrando usuario con id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario con id {} no encontrado", id);
                    throw new ResourceNotFoundException("User not found");
                });
        log.info("Usuario encontrado: {}", user.getEmail());
        return mapTUserResponse(user);
    }

    private UserResponse mapTUserResponse(User body) {
        return UserResponse.builder()
                .id(body.getId())
                .username(body.getProfile() != null ? body.getProfile().getUsername() : body.getEmail())
                .email(body.getEmail())
                .isActive(body.getIsActive())
                .role(Role.valueOf(body.getRole().getName()))
                .build();
    };

}
