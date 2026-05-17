package com.nexora.core.content.repository;

import com.nexora.core.content.entity.Follow;
import com.nexora.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
    long countByFollower(User follower);
    long countByFollowing(User following);
    void deleteByFollowerAndFollowing(User follower, User following);
}
