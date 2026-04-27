package com.nexora.core.content.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.core.content.entity.ResearchPaper;
import com.nexora.core.content.repository.ResearchPaperRepository;

@ExtendWith(MockitoExtension.class)
class ResearchPaperServiceImplTest {

    @Mock
    private ResearchPaperRepository repository;

    @InjectMocks
    private ResearchPaperServiceImpl researchService;

    @Test
    void save_GeneratesSlug() {
        ResearchPaper paper = new ResearchPaper();
        paper.setTitle("Optimización de Algoritmos en IA");
        
        when(repository.save(any(ResearchPaper.class))).thenAnswer(i -> i.getArguments()[0]);

        ResearchPaper savedPaper = researchService.save(paper);

        assertNotNull(savedPaper.getSlug());
        assertEquals("optimizacion-de-algoritmos-en-ia", savedPaper.getSlug());
        verify(repository, times(1)).save(paper);
    }

    @Test
    void incrementViews_Success() {
        String slug = "test-slug";
        ResearchPaper paper = new ResearchPaper();
        paper.setViews(10);
        
        when(repository.findBySlug(slug)).thenReturn(Optional.of(paper));
        when(repository.save(any(ResearchPaper.class))).thenReturn(paper);

        researchService.incrementViews(slug);

        assertEquals(11, paper.getViews());
        verify(repository, times(1)).save(paper);
    }
}
