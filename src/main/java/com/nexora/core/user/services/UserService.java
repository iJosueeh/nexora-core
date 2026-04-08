package com.nexora.core.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.user.dto.UserResponse;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(UUID id) {
        log.info("Encontrando usuario con id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario con id {} no encontrado", id);
                    throw new ResourceNotFoundException("User not found");
                });
        log.info("Usuario encontrado: {}", user.getUsername());
        return mapTUserResponse(user);
    }

    private UserResponse mapTUserResponse(User body) {
        return UserResponse.builder()
                .id(body.getId())
                .username(body.getUsername())
                .email(body.getEmail())
                .isActive(body.getIsActive())
                .role(body.getRole())
                .build();
    };

}
