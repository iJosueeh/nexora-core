package com.nexora.core.auth.services;

import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.nexora.core.auth.dto.*;
import com.nexora.core.security.jwt.JwtService;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfilesRepository profilesRepository;
    private final AcademicInterestsRepository academicInterestsRepository;
    private final ProfilesInterestsRepository profilesInterestsRepository;
    private final JwtService jwtService;
    private final WebClient supabaseWebClient;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthResponse registerStart(RegisterStartRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String supabaseId;
        // 1. Register in Supabase via Admin API
        try {
            Map<String, Object> response = supabaseWebClient.post()
                .uri("/admin/users")
                .bodyValue(Map.of(
                    "email", request.getEmail(),
                    "password", request.getPassword(),
                    "email_confirm", true
                ))
                .retrieve()
                .onStatus(status -> status.isError(), res -> 
                    res.bodyToMono(Map.class).map(body -> 
                        new RuntimeException("Supabase Admin Error: " + (body.get("message") != null ? body.get("message") : body.get("msg")))
                    )
                )
                .bodyToMono(Map.class)
                .block();
            
            supabaseId = (String) response.get("id");
        } catch (Exception e) {
            throw new RuntimeException("Error registering in Supabase (Admin): " + e.getMessage());
        }

        // 2. Register in local DB (or Update if trigger already created it)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(java.util.UUID.fromString(supabaseId));
                    newUser.setEmail(request.getEmail());
                    return newUser;
                });

        Roles userRole = roleRepository.findByName(Role.ROLE_STUDENT.name())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        user.setIsActive(true);
        user.setRole(userRole);

        User savedUser = userRepository.save(user);
        
        // 3. Initialize or Update empty profile
        Profiles profile = profilesRepository.findByUser_Id(savedUser.getId());
        if (profile == null) {
            profile = new Profiles();
            profile.setUser(savedUser);
            profile.setFollowersCount(0);
            profilesRepository.save(profile);
        }

        return buildAuthResponse(savedUser);
    }

    public AuthResponse completeRegistration(User user, RegisterUpdateRequest request) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        if (profile == null) {
            throw new RuntimeException("Profile not found for user");
        }

        // Update identity if provided
        if (request.getUsername() != null) profile.setUsername(request.getUsername());
        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        
        profilesRepository.save(profile);

        // Update preferences if provided
        if (request.getAcademicInterests() != null && request.getAcademicInterests().length > 0) {
            profilesInterestsRepository.deleteByProfile(profile);

            for (String interestName : request.getAcademicInterests()) {
                AcademicInterests interest = academicInterestsRepository.findByName(interestName)
                        .orElseGet(() -> {
                            AcademicInterests newInterest = new AcademicInterests();
                            newInterest.setName(interestName);
                            return academicInterestsRepository.save(newInterest);
                        });

                ProfilesInterests relation = new ProfilesInterests();
                relation.setProfile(profile);
                relation.setInteres(interest);
                profilesInterestsRepository.save(relation);
            }
        }

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate with Supabase
        try {
            supabaseWebClient.post()
                .uri("/token?grant_type=password")
                .bodyValue(Map.of(
                    "email", request.getEmail(),
                    "password", request.getPassword()
                ))
                .retrieve()
                .onStatus(status -> status.isError(), response -> 
                    response.bodyToMono(Map.class).map(body -> 
                        new RuntimeException("Login failed in Supabase: " + (body.get("error_description") != null ? body.get("error_description") : body.get("error")))
                    )
                )
                .toBodilessEntity()
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        // 2. If Supabase is happy, find local user and issue our JWT
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        //Buscar el perfil
        Profiles profile = profilesRepository.findByUser_Id(user.getId());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expiration)
                .userId(user.getId())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole().getName()))
                .username(profile != null ? profile.getUsername() : null)
                .fullName(profile != null ? profile.getFullName() : null)
                .build();
    }
}
