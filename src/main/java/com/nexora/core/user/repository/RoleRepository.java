package com.nexora.core.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexora.core.user.entity.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, UUID> {
    Optional<Roles> findByName(String name);
}
