package com.nexora.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import com.nexora.core.auth.dto.AuthResponse;
import com.nexora.core.auth.dto.LoginRequest;
import com.nexora.core.auth.dto.RegisterRequest;
import com.nexora.core.auth.services.AuthService;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(Role.ROLE_STUDENT.name()).isEmpty()) {
            Roles role = new Roles();
            role.setName(Role.ROLE_STUDENT.name());
            roleRepository.save(role);
        }
    }

    @Test
    void registerShouldReturnToken() {
        String email = "user-" + UUID.randomUUID() + "@utp.edu.pe";

        RegisterRequest request = new RegisterRequest();
        request.setUsername("john_doe");
        request.setEmail(email);
        request.setPassword("Password123");

        AuthResponse response = authService.register(request);

        assertNotNull(response.getAccessToken());
        assertEquals(email, response.getEmail());
    }

    @Test
    void loginWithWrongPasswordShouldFail() {
        String email = "user-" + UUID.randomUUID() + "@utp.edu.pe";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("john_doe");
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123");
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("WrongPassword123");

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
