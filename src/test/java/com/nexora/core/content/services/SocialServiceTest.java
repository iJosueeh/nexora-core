package com.nexora.core.content.services;

import com.nexora.core.auth.services.AuthService;
import com.nexora.core.content.entity.Follow;
import com.nexora.core.content.repository.FollowRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private ProfilesRepository profilesRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private AuthService authService;
    @Mock
    private ProfilesInterestsRepository profilesInterestsRepository;

    @InjectMocks
    private SocialService socialService;

    private UUID currentUserId;
    private UUID targetUserId;
    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        follower = new User();
        follower.setId(currentUserId);
        following = new User();
        following.setId(targetUserId);
    }

    @Test
    void toggleFollowShouldDeleteAndDecrementWhenAlreadyFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(true);

        boolean result = socialService.toggleFollow(targetUserId);

        assertFalse(result);
        verify(followRepository).deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
        verify(profilesRepository).decrementFollowingCount(currentUserId);
        verify(profilesRepository).decrementFollowersCount(targetUserId);
        verify(entityManager).flush();
    }

    @Test
    void toggleFollowShouldSaveAndIncrementWhenNotFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(false);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(following));

        boolean result = socialService.toggleFollow(targetUserId);

        assertTrue(result);
        verify(followRepository).save(any(Follow.class));
        verify(profilesRepository).incrementFollowingCount(currentUserId);
        verify(profilesRepository).incrementFollowersCount(targetUserId);
        verify(entityManager).flush();
    }

    @Test
    void toggleFollowShouldThrowExceptionWhenFollowingSelf() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(IllegalArgumentException.class, () -> socialService.toggleFollow(currentUserId));
        verifyNoInteractions(followRepository);
    }
}
