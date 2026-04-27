package com.nexora.core.content.service.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.content.entity.ResearchPaper;
import com.nexora.core.content.repository.ResearchPaperRepository;
import com.nexora.core.content.service.ResearchPaperService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResearchPaperServiceImpl implements ResearchPaperService {

    private final ResearchPaperRepository repository;

    @Override
    public List<ResearchPaper> findAll(int limit, int offset, String faculty) {
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        if (faculty != null && !faculty.equalsIgnoreCase("Todos")) {
            return repository.findByFacultyIgnoreCase(faculty, pageRequest).getContent();
        }
        return repository.findAll(pageRequest).getContent();
    }

    @Override
    public Optional<ResearchPaper> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }

    @Override
    @Transactional
    public ResearchPaper save(ResearchPaper paper) {
        if (paper.getSlug() == null || paper.getSlug().isEmpty()) {
            paper.setSlug(SlugUtils.makeSlug(paper.getTitle()));
        }
        return repository.save(paper);
    }

    @Override
    @Transactional
    public void incrementViews(String slug) {
        repository.findBySlug(slug).ifPresent(paper -> {
            paper.setViews(paper.getViews() + 1);
            repository.save(paper);
        });
    }
}
