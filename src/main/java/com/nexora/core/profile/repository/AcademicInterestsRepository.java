package com.nexora.core.profile.repository;

import com.nexora.core.profile.entity.AcademicInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface AcademicInterestsRepository extends JpaRepository<AcademicInterests, UUID> {
    Optional<AcademicInterests> findByName(String name);

    List<AcademicInterests> findAllByOrderByNameAsc();
}
