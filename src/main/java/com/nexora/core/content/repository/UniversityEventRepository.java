package com.nexora.core.content.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexora.core.content.entity.UniversityEvent;

@Repository
public interface UniversityEventRepository extends JpaRepository<UniversityEvent, UUID> {
    Optional<UniversityEvent> findBySlug(String slug);

    Page<UniversityEvent> findByCategoryIgnoreCase(String category, Pageable pageable);
}
