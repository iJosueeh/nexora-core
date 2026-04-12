package com.nexora.core.graphql;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.nexora.core.graphql.dto.CommentThreadView;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<FeedPostView> obtenerFeedPrincipal(int limit, int offset) {
        String sql = """
                SELECT
                    p.id,
                    p.titulo,
                    p.content AS contenido,
                    COALESCE(p.is_official, FALSE) AS is_official,
                    u.id AS autor_id,
                    COALESCE(pf.username, split_part(u.email, '@', 1)) AS autor_username,
                    pf.full_name AS autor_full_name,
                    pf.avatar_url AS autor_avatar_url
                FROM posts p
                JOIN usuarios u ON u.id = p.autor_id
                LEFT JOIN perfiles pf ON pf.usuario_id = u.id
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            FeedAuthorView autor = new FeedAuthorView(
                    rs.getObject("autor_id", UUID.class),
                    rs.getString("autor_username"),
                    rs.getString("autor_full_name"),
                    rs.getString("autor_avatar_url"));

            return new FeedPostView(
                    rs.getObject("id", UUID.class),
                    rs.getString("titulo"),
                    rs.getString("contenido"),
                    rs.getBoolean("is_official"),
                    autor);
        });
    }

    public List<CommentThreadView> obtenerHilosComentarios(UUID postId) {
        String sql = """
                SELECT
                    c.id,
                    c.post_id,
                    c.parent_id,
                    c.autor_id,
                    c.content AS contenido,
                    c.created_at
                FROM comentarios c
                WHERE c.post_id = :postId
                ORDER BY c.created_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId);

        List<CommentThreadView> comentarios = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Timestamp rawCreatedAt = rs.getTimestamp("created_at");
            String createdAt = null;
            if (rawCreatedAt != null) {
                LocalDateTime createdAtValue = rawCreatedAt.toLocalDateTime();
                createdAt = createdAtValue.toString();
            }

            return new CommentThreadView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("post_id", UUID.class),
                    rs.getObject("parent_id", UUID.class),
                    rs.getObject("autor_id", UUID.class),
                    rs.getString("contenido"),
                    createdAt);
        });

        Map<UUID, CommentThreadView> porId = new HashMap<>();
        for (CommentThreadView comentario : comentarios) {
            porId.put(comentario.getId(), comentario);
        }

        List<CommentThreadView> raices = new ArrayList<>();
        for (CommentThreadView comentario : comentarios) {
            UUID parentId = comentario.getParentId();
            if (parentId != null && porId.containsKey(parentId)) {
                porId.get(parentId).getRespuestas().add(comentario);
            } else {
                raices.add(comentario);
            }
        }

        return raices;
    }
}
