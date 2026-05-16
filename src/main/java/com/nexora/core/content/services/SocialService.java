package com.nexora.core.content.services;

import com.nexora.core.content.entity.Follow;
import com.nexora.core.content.repository.FollowRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProfilesRepository profilesRepository;
    private final SecurityService securityService;

    @Transactional
    public boolean toggleFollow(UUID targetUserId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        boolean alreadyFollowing = followRepository.existsByFollowerAndFollowing(follower, following);

        if (alreadyFollowing) {
            followRepository.deleteByFollowerAndFollowing(follower, following);
            
            // Update counters
            profilesRepository.decrementFollowingCount(currentUserId);
            profilesRepository.decrementFollowersCount(targetUserId);
            
            return false; // Unfollowed
        } else {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
            
            // Update counters
            profilesRepository.incrementFollowingCount(currentUserId);
            profilesRepository.incrementFollowersCount(targetUserId);
            
            return true; // Followed
        }
    }
}
