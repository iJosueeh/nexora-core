package com.nexora.core.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexora.core.profile.entity.Profiles;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, UUID> {

    Profiles findByUser_Id(UUID id);

    Optional<Profiles> findByUsernameIgnoreCase(String username);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Profiles p SET p.followersCount = p.followersCount + 1 WHERE p.user.id = :userId")
    void incrementFollowersCount(UUID userId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Profiles p SET p.followersCount = p.followersCount - 1 WHERE p.user.id = :userId AND p.followersCount > 0")
    void decrementFollowersCount(UUID userId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Profiles p SET p.followingCount = p.followingCount + 1 WHERE p.user.id = :userId")
    void incrementFollowingCount(UUID userId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Profiles p SET p.followingCount = p.followingCount - 1 WHERE p.user.id = :userId AND p.followingCount > 0")
    void decrementFollowingCount(UUID userId);
}
