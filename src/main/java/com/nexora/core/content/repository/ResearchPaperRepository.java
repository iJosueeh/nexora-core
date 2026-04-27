package com.nexora.core.content.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexora.core.content.entity.ResearchPaper;

@Repository
public interface ResearchPaperRepository extends JpaRepository<ResearchPaper, UUID> {
    Optional<ResearchPaper> findBySlug(String slug);
    
    Page<ResearchPaper> findByFacultyIgnoreCase(String faculty, Pageable pageable);
}
