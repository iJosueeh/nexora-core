package com.nexora.core.content.services;

import com.nexora.core.security.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private InteractionService interactionService;

    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
    }

    @Test
    void toggleLikeShouldDeleteWhenAlreadyLiked() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(true);

        boolean result = interactionService.toggleLike(postId);

        assertFalse(result);
        verify(jdbcTemplate).update(contains("DELETE FROM post_likes"), any(MapSqlParameterSource.class));
    }

    @Test
    void toggleLikeShouldInsertWhenNotLiked() {
        when(securityService.getCurrentUserId()).thenReturn(userId);
        when(jdbcTemplate.queryForObject(any(String.class), any(MapSqlParameterSource.class), eq(Boolean.class)))
                .thenReturn(false);

        boolean result = interactionService.toggleLike(postId);

        assertTrue(result);
        verify(jdbcTemplate).update(contains("INSERT INTO post_likes"), any(MapSqlParameterSource.class));
    }
}
