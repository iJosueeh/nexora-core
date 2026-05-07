package com.nexora.core.content.services;

import com.nexora.core.security.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SecurityService securityService;

    @Transactional
    public boolean toggleLike(UUID postId) {
        UUID currentUserId = securityService.getCurrentUserId();

        String checkSql = "SELECT EXISTS(SELECT 1 FROM post_likes WHERE post_id = :postId AND user_id = :userId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("userId", currentUserId);

        Boolean alreadyLiked = jdbcTemplate.queryForObject(checkSql, params, Boolean.class);

        if (Boolean.TRUE.equals(alreadyLiked)) {
            String deleteSql = "DELETE FROM post_likes WHERE post_id = :postId AND user_id = :userId";
            jdbcTemplate.update(deleteSql, params);
            return false; // Liked removed
        } else {
            String insertSql = "INSERT INTO post_likes (post_id, user_id) VALUES (:postId, :userId)";
            jdbcTemplate.update(insertSql, params);
            return true; // Liked added
        }
    }
}
