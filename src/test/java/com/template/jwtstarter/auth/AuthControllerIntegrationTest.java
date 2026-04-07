package com.template.jwtstarter.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

import com.template.jwtstarter.auth.dto.AuthResponse;
import com.template.jwtstarter.auth.dto.LoginRequest;
import com.template.jwtstarter.auth.dto.RegisterRequest;
import com.template.jwtstarter.auth.services.AuthService;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerShouldReturnToken() {
        String email = "user-" + UUID.randomUUID() + "@example.com";

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
        String email = "user-" + UUID.randomUUID() + "@example.com";

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
