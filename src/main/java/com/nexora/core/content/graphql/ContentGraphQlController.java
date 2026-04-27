package com.nexora.core.content.graphql;

import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import com.nexora.core.content.entity.ResearchPaper;
import com.nexora.core.content.entity.UniversityEvent;
import com.nexora.core.content.service.ResearchPaperService;
import com.nexora.core.content.service.UniversityEventService;
import com.nexora.core.user.entity.User;
import com.nexora.core.security.service.SecurityService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentGraphQlController {

    private final ResearchPaperService researchService;
    private final UniversityEventService eventService;
    private final SecurityService securityService;

    @QueryMapping
    public List<ResearchPaper> researchPapers(@Argument int limit, @Argument int offset, @Argument String faculty) {
        return researchService.findAll(limit, offset, faculty);
    }

    @QueryMapping
    public ResearchPaper researchBySlug(@Argument String slug) {
        ResearchPaper paper = researchService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Investigación no encontrada"));
        researchService.incrementViews(slug);
        return paper;
    }

    @QueryMapping
    public List<UniversityEvent> universityEvents(@Argument int limit, @Argument int offset, @Argument String category) {
        return eventService.findAll(limit, offset, category);
    }

    @QueryMapping
    public UniversityEvent eventBySlug(@Argument String slug) {
        return eventService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    @MutationMapping
    public UniversityEvent confirmRSVP(@Argument UUID eventId) {
        UUID userId = securityService.getCurrentUserId();
        return eventService.confirmRSVP(eventId, userId);
    }

    @SchemaMapping(typeName = "UniversityEvent", field = "isUserRegistered")
    public boolean isUserRegistered(UniversityEvent event) {
        try {
            UUID userId = securityService.getCurrentUserId();
            return eventService.isUserRegistered(event.getId(), userId);
        } catch (Exception e) {
            return false;
        }
    }

    @SchemaMapping(typeName = "UniversityEvent", field = "attendeesCount")
    public int attendeesCount(UniversityEvent event) {
        return event.getAttendees().size();
    }
}
