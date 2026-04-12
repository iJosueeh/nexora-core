package com.nexora.core.auth.services;

import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.auth.dto.AuthResponse;
import com.nexora.core.auth.dto.LoginRequest;
import com.nexora.core.auth.dto.RegisterRequest;
import com.nexora.core.security.jwt.JwtService;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfilesRepository profilesRepository;
    private final AcademicInterestsRepository academicInterestsRepository;
    private final ProfilesInterestsRepository profilesInterestsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Roles userRole = roleRepository.findByName(Role.ROLE_STUDENT.name())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setRole(userRole);

        User savedUser = userRepository.save(user);
        //Perfil
        Profiles profile = new Profiles();
        profile.setUsuarioId(savedUser.getId());
        profile.setCarrera(null);
        profile.setUsername(request.getUsername());
        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        profile.setAvatarUrl(null);
        profile.setBannerUrl(null);
        profile.setFollowersCount(0);

        Profiles savedProfile = profilesRepository.save(profile);

        //Intereses de Perfil
        for (String interestName : request.getAcademicInterests()) {
            // Buscar si el interés existe en la DB (o crearlo si no)
            AcademicInterests interest = academicInterestsRepository.findByName(interestName)
                    .orElseGet(() -> {
                        AcademicInterests newInterest = new AcademicInterests();
                        newInterest.setName(interestName);
                        return academicInterestsRepository.save(newInterest);
                    });

            // Crear la relación en la tabla intermedia
            ProfilesInterests relation = new ProfilesInterests();
            relation.setProfile(savedProfile);
            relation.setInteres(interest);
            profilesInterestsRepository.save(relation);
        }
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        //Buscar el perfil

        Profiles profile = profilesRepository.findByUsuarioId(user.getId());

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
