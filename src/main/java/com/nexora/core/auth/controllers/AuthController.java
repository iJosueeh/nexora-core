package com.nexora.core.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.core.auth.dto.AuthResponse;
import com.nexora.core.auth.dto.LoginRequest;
import com.nexora.core.auth.dto.RegisterStartRequest;
import com.nexora.core.auth.dto.RegisterIdentityRequest;
import com.nexora.core.auth.dto.RegisterPreferencesRequest;
import com.nexora.core.auth.dto.RefreshRequest;
import com.nexora.core.auth.services.AuthService;
import com.nexora.core.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Step 1: Account (Public - Returns Tokens)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterStartRequest request) {
        AuthResponse authResponse = authService.registerStart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    // Step 2: Identity (Protected - Requires Token from Step 1)
    @PutMapping("/register/identity")
    public ResponseEntity<AuthResponse> registerIdentity(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterIdentityRequest request) {
        AuthResponse authResponse = authService.registerIdentity(user, request);
        return ResponseEntity.ok(authResponse);
    }

    // Step 3: Preferences (Protected - Requires Token from Step 1)
    @PutMapping("/register/preferences")
    public ResponseEntity<AuthResponse> registerPreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterPreferencesRequest request) {
        AuthResponse authResponse = authService.registerPreferences(user, request);
        return ResponseEntity.ok(authResponse);
    }

    // Login (Public)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    // Refresh Token (Public)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse authResponse = authService.refresh(request);
        return ResponseEntity.ok(authResponse);
    }
}
