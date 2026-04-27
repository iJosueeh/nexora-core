package com.nexora.core.content.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nexora.core.content.entity.UniversityEvent;
import com.nexora.core.content.repository.UniversityEventRepository;
import com.nexora.core.content.service.UniversityEventService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UniversityEventServiceImpl implements UniversityEventService {

    private final UniversityEventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<UniversityEvent> findAll(int limit, int offset, String category) {
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("date").descending());

        if (category != null && !category.equalsIgnoreCase("Todos")) {
            return eventRepository.findByCategoryIgnoreCase(category, pageRequest).getContent();
        }

        return eventRepository.findAll(pageRequest).getContent();
    }


    @Override
    public Optional<UniversityEvent> findBySlug(String slug) {
        return eventRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public UniversityEvent confirmRSVP(UUID eventId, UUID userId) {
        UniversityEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (event.getAttendees().contains(user)) {
            throw new RuntimeException("Ya estás registrado en este evento");
        }

        event.getAttendees().add(user);
        return eventRepository.save(event);
    }

    @Override
    public boolean isUserRegistered(UUID eventId, UUID userId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getAttendees().stream()
                        .anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }
}
