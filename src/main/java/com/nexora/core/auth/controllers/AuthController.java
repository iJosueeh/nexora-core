package com.nexora.core.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.core.auth.dto.AuthResponse;
import com.nexora.core.auth.dto.RegistrationCatalogsResponse;
import com.nexora.core.auth.dto.RegisterUpdateRequest;
import com.nexora.core.auth.services.AuthService;
import com.nexora.core.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PutMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> completeRegistration(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid RegisterUpdateRequest request) {

        String email = jwt.getClaimAsString("email");
        AuthResponse authResponse = authService.completeRegistration(email, request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Registration information updated")
                .data(authResponse)
                .build());
    }

    @GetMapping("/catalogs")
    public ResponseEntity<ApiResponse<RegistrationCatalogsResponse>> registrationCatalogs() {
        RegistrationCatalogsResponse catalogs = authService.getRegistrationCatalogs();

        return ResponseEntity.ok(ApiResponse.<RegistrationCatalogsResponse>builder()
                .success(true)
                .message("Registration catalogs loaded")
                .data(catalogs)
                .build());
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<AuthResponse>> session(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String supabaseUserId = jwt.getSubject();
        AuthResponse authResponse = authService.resolveSession(email, supabaseUserId);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Session resolved")
                .data(authResponse)
                .build());
    }
}
