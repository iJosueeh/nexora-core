package com.nexora.core.content.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nexora.core.content.entity.UniversityEvent;

public interface UniversityEventService {
    List<UniversityEvent> findAll(int limit, int offset, String category);
    Optional<UniversityEvent> findBySlug(String slug);
    UniversityEvent confirmRSVP(UUID eventId, UUID userId);
    boolean isUserRegistered(UUID eventId, UUID userId);
}
