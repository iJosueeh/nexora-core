package com.nexora.core.content.services;

import com.nexora.core.auth.services.AuthService;
import com.nexora.core.content.entity.Follow;
import com.nexora.core.content.repository.FollowRepository;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowRepository followRepository;
    private final ProfilesRepository profilesRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final EntityManager entityManager;
    private final AuthService authService;
    private final ProfilesInterestsRepository profilesInterestsRepository;

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowers(UUID userId) {
        return followRepository.findFollowersByFollowingId(userId).stream()
                .map(this::mapToProfileView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowing(UUID userId) {
        return followRepository.findFollowingByFollowerId(userId).stream()
                .map(this::mapToProfileView)
                .toList();
    }

    private ProfileView mapToProfileView(User user) {
        Profiles profile = profilesRepository.findByUser_Id(user.getId());
        long interestsCount = profilesInterestsRepository.countByProfile(profile);
        return authService.buildProfileView(user, profile, interestsCount);
    }

    @Transactional
    public boolean toggleFollow(UUID targetUserId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);

        if (alreadyFollowing) {
            followRepository.deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
            updateCounters(currentUserId, targetUserId, -1);
            entityManager.flush();
            return false;
        } else {
            User follower = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));
            User following = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);

            updateCounters(currentUserId, targetUserId, 1);
            entityManager.flush();
            return true;
        }
    }

    private void updateCounters(UUID followerUserId, UUID followingUserId, int delta) {
        if (delta > 0) {
            profilesRepository.incrementFollowingCount(followerUserId);
            profilesRepository.incrementFollowersCount(followingUserId);
        } else {
            profilesRepository.decrementFollowingCount(followerUserId);
            profilesRepository.decrementFollowersCount(followingUserId);
        }
    }
}
