package com.template.jwtstarter.user.services;

import org.springframework.stereotype.Service;

import com.template.jwtstarter.common.exception.ResourceNotFoundException;
import com.template.jwtstarter.user.dto.UserResponse;
import com.template.jwtstarter.user.entity.User;
import com.template.jwtstarter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(Long id) {
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
