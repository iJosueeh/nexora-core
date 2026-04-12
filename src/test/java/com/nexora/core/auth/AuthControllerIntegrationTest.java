package com.nexora.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.auth.dto.AuthResponse;
import com.nexora.core.auth.dto.LoginRequest;
import com.nexora.core.auth.dto.RegisterRequest;
import com.nexora.core.auth.services.AuthService;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private ProfilesInterestsRepository profilesInterestsRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(Role.ROLE_STUDENT.name()).isEmpty()) {
            Roles role = new Roles();
            role.setName(Role.ROLE_STUDENT.name());
            roleRepository.save(role);
        }
    }

    @Test
    void registerShouldCreateProfileAndReturnToken() {
        String email = "user-" + UUID.randomUUID() + "@utp.edu.pe";
        String username = "testuser_" + UUID.randomUUID().toString().substring(0, 8);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("Password123!");
        request.setUsername(username);
        request.setFullName("Test User");
        request.setBio("This is a test biography for integration testing.");
        request.setAcademicInterests(new String[]{"Java", "Spring Boot", "Testing"});

        AuthResponse response = authService.register(request);

        // Verify Response
        assertNotNull(response.getAccessToken());
        assertEquals(email, response.getEmail());
        assertEquals(username, response.getUsername());
        assertEquals("Test User", response.getFullName());

        // Verify Database Persistence
        Profiles profile = profilesRepository.findByUsuarioId(response.getUserId());
        assertNotNull(profile);
        assertEquals(username, profile.getUsername());
        assertEquals("Test User", profile.getFullName());
        
        // Verify Interests (count should match)
        long interestsCount = profilesInterestsRepository.findAll().stream()
                .filter(pi -> pi.getProfile().getId().equals(profile.getId()))
                .count();
        assertEquals(3, interestsCount);
    }

    @Test
    void loginShouldReturnTokenAndProfileInfo() {
        String email = "login-" + UUID.randomUUID() + "@utp.edu.pe";
        String username = "loginuser";

        // 1. Register first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setUsername(username);
        registerRequest.setFullName("Login Success User");
        registerRequest.setBio("Bio for login test");
        registerRequest.setAcademicInterests(new String[]{"Security"});
        authService.register(registerRequest);

        // 2. Perform Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123!");

        AuthResponse response = authService.login(loginRequest);

        // 3. Verify Response
        assertNotNull(response.getAccessToken());
        assertEquals(email, response.getEmail());
        assertEquals(username, response.getUsername());
        assertEquals("Login Success User", response.getFullName());
    }

    @Test
    void loginWithWrongPasswordShouldFail() {
        String email = "fail-" + UUID.randomUUID() + "@utp.edu.pe";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("CorrectPassword123!");
        registerRequest.setUsername("fail_user");
        registerRequest.setFullName("Fail User");
        registerRequest.setBio("Bio");
        registerRequest.setAcademicInterests(new String[]{"Misc"});
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("WrongPassword123!");

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
