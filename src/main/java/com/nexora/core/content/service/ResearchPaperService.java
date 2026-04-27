package com.nexora.core.content.service;

import java.util.List;
import java.util.Optional;
import com.nexora.core.content.entity.ResearchPaper;

public interface ResearchPaperService {
    List<ResearchPaper> findAll(int limit, int offset, String faculty);
    Optional<ResearchPaper> findBySlug(String slug);
    ResearchPaper save(ResearchPaper paper);
    void incrementViews(String slug);
}
