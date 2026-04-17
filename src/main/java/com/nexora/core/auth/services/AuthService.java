package com.nexora.core.auth.services;

import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.nexora.core.auth.dto.*;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfilesRepository profilesRepository;
    private final AcademicInterestsRepository academicInterestsRepository;
    private final ProfilesInterestsRepository profilesInterestsRepository;
    private final WebClient supabaseWebClient;

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

        // 2. Register in local DB
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
        
        // 3. Initialize empty profile
        Profiles profile = profilesRepository.findByUser_Id(savedUser.getId());
        if (profile == null) {
            profile = new Profiles();
            profile.setUser(savedUser);
            profile.setFollowersCount(0);
            profilesRepository.save(profile);
        }

        // 4. AUTOMATIC LOGIN: Return tokens immediately so user can proceed to Step 2 & 3
        return login(new LoginRequest() {{
            setEmail(request.getEmail());
            setPassword(request.getPassword());
        }});
    }

    public AuthResponse registerIdentity(User user, RegisterIdentityRequest request) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        if (profile == null) {
            throw new RuntimeException("Profile not found for user: " + user.getEmail());
        }

        profile.setUsername(request.getUsername());
        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        
        profilesRepository.save(profile);

        return buildMinimalAuthResponse(user);
    }

    public AuthResponse registerPreferences(User user, RegisterPreferencesRequest request) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        if (profile == null) {
            throw new RuntimeException("Profile not found for user: " + user.getEmail());
        }

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

        return buildMinimalAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Map<String, Object> supabaseResponse;
        try {
            supabaseResponse = supabaseWebClient.post()
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
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database"));

        return buildAuthResponse(user, supabaseResponse);
    }

    public AuthResponse refresh(RefreshRequest request) {
        Map<String, Object> supabaseResponse;
        try {
            supabaseResponse = supabaseWebClient.post()
                .uri("/token?grant_type=refresh_token")
                .bodyValue(Map.of(
                    "refresh_token", request.getRefreshToken()
                ))
                .retrieve()
                .onStatus(status -> status.isError(), response -> 
                    response.bodyToMono(Map.class).map(body -> 
                        new RuntimeException("Refresh failed in Supabase: " + (body.get("error_description") != null ? body.get("error_description") : body.get("error")))
                    )
                )
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }

        Map<String, Object> userMap = (Map<String, Object>) supabaseResponse.get("user");
        String email = (String) userMap.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database after refresh"));

        return buildAuthResponse(user, supabaseResponse);
    }

    private AuthResponse buildAuthResponse(User user, Map<String, Object> supabaseResponse) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        boolean profileComplete = profile != null && profile.getCarrera() != null;

        return AuthResponse.builder()
                .accessToken((String) supabaseResponse.get("access_token"))
                .refreshToken((String) supabaseResponse.get("refresh_token"))
                .expiresIn(((Number) supabaseResponse.get("expires_in")).longValue())
                .user(AuthResponse.UserData.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(profile != null ? profile.getUsername() : null)
                        .profileComplete(profileComplete)
                        .build())
                .build();
    }

    private AuthResponse buildMinimalAuthResponse(User user) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        boolean profileComplete = profile != null && profile.getCarrera() != null;
        
        return AuthResponse.builder()
                .user(AuthResponse.UserData.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(profile != null ? profile.getUsername() : null)
                        .profileComplete(profileComplete)
                        .build())
                .build();
    }
}
