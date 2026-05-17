package com.nexora.core.content.repository;

import com.nexora.core.content.entity.Follow;
import com.nexora.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    long countByFollower(User follower);
    long countByFollowing(User following);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
