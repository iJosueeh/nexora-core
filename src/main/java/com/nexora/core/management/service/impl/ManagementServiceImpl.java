package com.nexora.core.management.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.content.entity.Post;
import com.nexora.core.content.repository.PostRepository;
import com.nexora.core.content.repository.UniversityEventRepository;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.management.graphql.dto.AdminStatsView;
import com.nexora.core.management.graphql.dto.RecentActivityView;
import com.nexora.core.management.service.ManagementService;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementServiceImpl implements ManagementService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UniversityEventRepository eventRepository;
    private final ProfilesRepository profilesRepository;
    private final SecurityService securityService;

    @Override
    public AdminStatsView getAdminStats() {
        long totalUsers = 0;
        long totalPosts = 0;
        long activeEvents = 0;

        try {
            totalUsers = userRepository.count();
        } catch (Exception e) {
            log.error("Error counting users: {}", e.getMessage());
        }

        try {
            totalPosts = postRepository.count();
        } catch (Exception e) {
            log.error("Error counting posts: {}", e.getMessage());
        }

        try {
            activeEvents = eventRepository.count();
        } catch (Exception e) {
            log.warn("University events table might be missing: {}", e.getMessage());
        }

        List<RecentActivityView> recentActivity = new ArrayList<>();
        try {
            recentActivity = postRepository.findAll().stream()
                    .limit(5)
                    .map(post -> {
                        OffsetDateTime date = post.getCreatedAt() != null 
                            ? post.getCreatedAt().atOffset(ZoneOffset.UTC) 
                            : OffsetDateTime.now(ZoneOffset.UTC);
                            
                        return RecentActivityView.builder()
                            .id(post.getId())
                            .type("POST_CREATED")
                            .description("Nueva publicación: " + post.getTitulo())
                            .createdAt(date)
                            .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent activity: {}", e.getMessage());
        }

        return AdminStatsView.builder()
                .totalUsers((int) totalUsers)
                .totalPosts((int) totalPosts)
                .activeEvents((int) activeEvents)
                .recentActivity(recentActivity)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileView> getAllUsers(int limit, int offset, String search) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        
        if (search != null && !search.isEmpty()) {
            return userRepository.findByEmailContainingIgnoreCase(search, pageRequest).stream()
                    .map(this::mapToProfileView)
                    .collect(Collectors.toList());
        }
        
        return userRepository.findAll(pageRequest).stream()
                .map(this::mapToProfileView)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfileView updateUserStatus(UUID userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setIsActive(isActive);
        User savedUser = userRepository.save(user);
        return mapToProfileView(savedUser);
    }

    @Override
    @Transactional
    public FeedPostView markPostAsOfficial(UUID postId, boolean isOfficial) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        post.setIsOfficial(isOfficial);
        Post savedPost = postRepository.save(post);
        return mapToFeedPostView(savedPost);
    }

    @Override
    @Transactional
    public boolean deletePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        
        UUID currentUserId = securityService.getCurrentUserId();

        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !post.getAutor().getId().equals(currentUserId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta publicación");
        }

        postRepository.delete(post);
        return true;
    }

    private ProfileView mapToProfileView(User user) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        return new ProfileView(
                user.getId(),
                user.getEmail(),
                profile != null ? profile.getUsername() : null,
                profile != null ? profile.getFullName() : "Sin nombre",
                profile != null ? profile.getBio() : null,
                (profile != null && profile.getCarrera() != null) ? profile.getCarrera().getName() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getBannerUrl() : null,
                profile != null ? profile.getFollowersCount() : 0,
                profile != null ? profile.getFollowingCount() : 0,
                new ArrayList<>(),
                user.getIsActive(),
                false
        );
    }

    private FeedPostView mapToFeedPostView(Post post) {
        Profiles profile = profilesRepository.findByUser_Id(post.getAutor().getId());
        FeedAuthorView autor = new FeedAuthorView(
                post.getAutor().getId(),
                profile != null ? profile.getUsername() : null,
                profile != null ? profile.getFullName() : "Sin nombre",
                profile != null ? profile.getAvatarUrl() : null
        );

        return new FeedPostView(
                post.getId(),
                post.getTitulo(),
                post.getContent(),
                post.getIsOfficial(),
                post.getCreatedAt().atOffset(ZoneOffset.UTC),
                0, 
                0, 
                false, 
                autor,
                post.getTags(),
                post.getLocation(),
                post.getImageUrl()
        );
    }
}
