package com.nexora.core.profile.repository;

import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfilesInterestsRepository extends JpaRepository<ProfilesInterests, UUID> {
    void deleteByProfile(Profiles profile);
}
