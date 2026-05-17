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
public class SocialService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SecurityService securityService;

    @Transactional
    public boolean toggleFollow(UUID targetUserId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        String checkSql = "SELECT EXISTS(SELECT 1 FROM seguidores WHERE follower_id = :followerId AND following_id = :followingId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("followerId", currentUserId)
                .addValue("followingId", targetUserId);

        Boolean alreadyFollowing = jdbcTemplate.queryForObject(checkSql, params, Boolean.class);

        if (Boolean.TRUE.equals(alreadyFollowing)) {
            String deleteSql = "DELETE FROM seguidores WHERE follower_id = :followerId AND following_id = :followingId";
            jdbcTemplate.update(deleteSql, params);
            
            updateCounters(currentUserId, targetUserId, -1);
            return false; 
        } else {
            String insertSql = "INSERT INTO seguidores (id, follower_id, following_id, created_at) " +
                               "VALUES (:id, :followerId, :followingId, NOW())";
            params.addValue("id", UUID.randomUUID());
            jdbcTemplate.update(insertSql, params);
            
            updateCounters(currentUserId, targetUserId, 1);
            return true;
        }
    }

    private void updateCounters(UUID followerUserId, UUID followingUserId, int delta) {
        String updateFollowingSql = "UPDATE perfiles SET following_count = GREATEST(0, following_count + :delta) WHERE usuario_id = :userId";
        String updateFollowersSql = "UPDATE perfiles SET followers_count = GREATEST(0, followers_count + :delta) WHERE usuario_id = :userId";

        jdbcTemplate.update(updateFollowingSql, new MapSqlParameterSource()
                .addValue("delta", delta)
                .addValue("userId", followerUserId));

        jdbcTemplate.update(updateFollowersSql, new MapSqlParameterSource()
                .addValue("delta", delta)
                .addValue("userId", followingUserId));
    }
}
