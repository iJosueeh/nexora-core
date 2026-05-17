package com.nexora.core.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticación", description = "Endpoints para la autenticación de usuarios y gestión de sesiones")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Completar registro de usuario", description = "Actualiza la información del perfil e intereses de un nuevo usuario.")
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

    @Operation(summary = "Obtener catálogos de registro", description = "Recupera las carreras e intereses académicos necesarios para el registro.")
    @GetMapping("/catalogs")
    public ResponseEntity<ApiResponse<RegistrationCatalogsResponse>> registrationCatalogs() {
        RegistrationCatalogsResponse catalogs = authService.getRegistrationCatalogs();

        return ResponseEntity.ok(ApiResponse.<RegistrationCatalogsResponse>builder()
                .success(true)
                .message("Registration catalogs loaded")
                .data(catalogs)
                .build());
    }

    @Operation(summary = "Obtener sesión actual", description = "Resuelve la información de la sesión para el usuario autenticado actualmente.")
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

    @Operation(summary = "Obtener perfil público", description = "Recupera la información del perfil público para un nombre de usuario dado.")
    @GetMapping("/public-profile/{username}")
    public ResponseEntity<ApiResponse<AuthResponse>> publicProfile(
            @PathVariable String username,
            @AuthenticationPrincipal Jwt jwt) {
        
        String viewerEmail = jwt != null ? jwt.getClaimAsString("email") : null;
        AuthResponse authResponse = authService.resolvePublicProfile(username, viewerEmail);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Public profile resolved")
                .data(authResponse)
                .build());
    }
}

