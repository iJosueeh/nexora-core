package com.nexora.core.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexora.core.profile.entity.Profiles;

import java.util.UUID;

@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, UUID> {

    Profiles findByUsuarioId(UUID id);
}
