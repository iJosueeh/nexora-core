package com.nexora.core.content.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.core.content.entity.UniversityEvent;
import com.nexora.core.content.repository.UniversityEventRepository;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UniversityEventServiceImplTest {

    @Mock
    private UniversityEventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UniversityEventServiceImpl eventService;

    private UUID eventId;
    private UUID userId;
    private UniversityEvent event;
    private User user;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        event = new UniversityEvent();
        event.setId(eventId);
        event.setAttendees(new HashSet<>());
        
        user = new User();
        user.setId(userId);
    }

    @Test
    void confirmRSVP_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.save(any(UniversityEvent.class))).thenReturn(event);

        UniversityEvent updatedEvent = eventService.confirmRSVP(eventId, userId);

        assertNotNull(updatedEvent);
        assertTrue(updatedEvent.getAttendees().contains(user));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void confirmRSVP_AlreadyRegistered_ThrowsException() {
        event.getAttendees().add(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.confirmRSVP(eventId, userId);
        });

        assertEquals("Ya estás registrado en este evento", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void confirmRSVP_EventNotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            eventService.confirmRSVP(eventId, userId);
        });
    }
}
