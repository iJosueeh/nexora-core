package com.nexora.core.auth.services;

import com.nexora.core.profile.entity.AcademicInterests;
import com.nexora.core.profile.entity.Courses;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.AcademicInterestsRepository;
import com.nexora.core.profile.repository.CoursesRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.auth.dto.*;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfilesRepository profilesRepository;
    private final AcademicInterestsRepository academicInterestsRepository;
    private final CoursesRepository coursesRepository;
    private final ProfilesInterestsRepository profilesInterestsRepository;
 

    public RegistrationCatalogsResponse getRegistrationCatalogs() {
        List<String> careers = coursesRepository.findAllByOrderByNameAsc()
                .stream()
                .map(Courses::getName)
                .toList();

        List<String> academicInterests = academicInterestsRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AcademicInterests::getName)
                .toList();

        return RegistrationCatalogsResponse.builder()
                .careers(careers)
                .academicInterests(academicInterests)
                .build();
    }

    public AuthResponse completeRegistration(String email, RegisterUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database"));

        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        if (profile == null) {
            throw new RuntimeException("Profile not found for user");
        }

        // Update identity if provided
        if (request.getUsername() != null) profile.setUsername(request.getUsername());
        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getBio() != null) profile.setBio(request.getBio());

        if (request.getCareer() != null && !request.getCareer().isBlank()) {
            Courses career = coursesRepository.findByNameIgnoreCase(request.getCareer().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Career not found: " + request.getCareer()));
            profile.setCarrera(career);
        }
        
        profilesRepository.save(profile);

        // Update preferences if provided
        String[] incomingInterests = resolveIncomingInterests(request);
        if (incomingInterests.length > 0) {
            profilesInterestsRepository.deleteByProfile(profile);

            for (String interestName : incomingInterests) {
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

        return buildSessionResponse(user);
    }

    public AuthResponse resolveSession(String email, String supabaseUserId) {
        User user = upsertUserFromSupabase(email, supabaseUserId);
        ensureProfileExists(user);
        return buildSessionResponse(user);
    }

    public AuthResponse resolvePublicProfile(String username) {
        Profiles profile = profilesRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found"));

        User user = profile.getUser();
        long interestsCount = profilesInterestsRepository.countByProfile(profile);
        List<String> academicInterests = mapAcademicInterests(profile);

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType(null)
                .expiresIn(0)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole() != null ? Role.valueOf(user.getRole().getName()) : null)
                .username(profile.getUsername())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .career(profile.getCarrera() != null ? profile.getCarrera().getName() : null)
                .avatarUrl(profile.getAvatarUrl())
                .bannerUrl(profile.getBannerUrl())
                .followersCount(profile.getFollowersCount())
                .academicInterests(academicInterests)
                .profileComplete(isProfileComplete(profile, interestsCount))
                .build();
    }

    private AuthResponse buildSessionResponse(User user) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        long interestsCount = profile == null ? 0 : profilesInterestsRepository.countByProfile(profile);
        List<String> academicInterests = mapAcademicInterests(profile);

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType(null)
                .expiresIn(0)
                .userId(user.getId())
                .email(user.getEmail())
                .role(Role.valueOf(user.getRole().getName()))
                .username(profile != null ? profile.getUsername() : null)
                .fullName(profile != null ? profile.getFullName() : null)
            .bio(profile != null ? profile.getBio() : null)
            .career(profile != null && profile.getCarrera() != null ? profile.getCarrera().getName() : null)
            .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
            .bannerUrl(profile != null ? profile.getBannerUrl() : null)
            .followersCount(profile != null ? profile.getFollowersCount() : 0)
            .academicInterests(academicInterests)
                .profileComplete(isProfileComplete(profile, interestsCount))
                .build();
    }

    private List<String> mapAcademicInterests(Profiles profile) {
        if (profile == null) return List.of();

        return profilesInterestsRepository.findAllByProfile(profile)
                .stream()
                .map(relation -> relation.getInteres().getName())
                .toList();
    }

    private User upsertUserFromSupabase(String email, String supabaseUserId) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    if (existing.getRole() == null) {
                        existing.setRole(resolveDefaultRole());
                    }
                    if (existing.getIsActive() == null) {
                        existing.setIsActive(true);
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setIsActive(true);
                    newUser.setRole(resolveDefaultRole());

                    if (supabaseUserId != null && !supabaseUserId.isBlank()) {
                        try {
                            newUser.setId(UUID.fromString(supabaseUserId));
                        } catch (IllegalArgumentException ignored) {
                            // If sub is not UUID-compatible, let JPA/base entity assign one.
                        }
                    }

                    return userRepository.save(newUser);
                });
    }

    private Roles resolveDefaultRole() {
        return roleRepository.findByName(Role.ROLE_STUDENT.name())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }

    private void ensureProfileExists(User user) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        if (profile != null) {
            return;
        }

        Profiles newProfile = new Profiles();
        newProfile.setUser(user);
        newProfile.setFollowersCount(0);
        profilesRepository.save(newProfile);
    }

    private boolean isProfileComplete(Profiles profile, long interestsCount) {
        if (profile == null) {
            return false;
        }

        return isFilled(profile.getUsername())
                && isFilled(profile.getFullName())
                && isFilled(profile.getBio())
                && profile.getCarrera() != null
                && interestsCount > 0;
    }

    private String[] resolveIncomingInterests(RegisterUpdateRequest request) {
        String[] source = request.getAcademicInterests();
        if (source == null || source.length == 0) {
            source = request.getSelectedInterests();
        }

        if (source == null || source.length == 0) {
            return new String[0];
        }

        return Arrays.stream(source)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }

    private boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
