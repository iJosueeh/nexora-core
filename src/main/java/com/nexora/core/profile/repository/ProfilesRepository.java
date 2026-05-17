package com.nexora.core.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.nexora.core.profile.entity.Profiles;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, UUID> {

    Profiles findByUser_Id(UUID id);

    Optional<Profiles> findByUsernameIgnoreCase(String username);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET followers_count = followers_count + 1 WHERE usuario_id = :userId", nativeQuery = true)
    void incrementFollowersCount(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET followers_count = GREATEST(0, followers_count - 1) WHERE usuario_id = :userId", nativeQuery = true)
    void decrementFollowersCount(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET following_count = following_count + 1 WHERE usuario_id = :userId", nativeQuery = true)
    void incrementFollowingCount(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET following_count = GREATEST(0, following_count - 1) WHERE usuario_id = :userId", nativeQuery = true)
    void decrementFollowingCount(UUID userId);
}
