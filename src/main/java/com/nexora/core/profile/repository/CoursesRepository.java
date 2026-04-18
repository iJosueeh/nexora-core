package com.nexora.core.profile.repository;

import com.nexora.core.profile.entity.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoursesRepository extends JpaRepository<Courses, UUID> {
    Optional<Courses> findByNameIgnoreCase(String name);

    List<Courses> findAllByOrderByNameAsc();
}