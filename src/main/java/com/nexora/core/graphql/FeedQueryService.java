package com.nexora.core.graphql;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.nexora.core.graphql.dto.CommentThreadView;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.TagSuggestionView;
import com.nexora.core.security.service.SecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SecurityService securityService;
    private static final List<String> TAG_COLUMN_CANDIDATES = List.of("tag", "tag_name", "tags", "name");
    
    private static final String FEED_SELECT_BASE_SQL = """
            SELECT
                p.id,
                p.titulo,
                p.content AS contenido,
                p.location,
                COALESCE(p.is_official, FALSE) AS is_official,
                p.created_at,
                (
                    SELECT COUNT(*)
                    FROM comentarios c
                    WHERE c.post_id = p.id
                ) AS comments_count,
                (
                    SELECT COUNT(*)
                    FROM post_likes l
                    WHERE l.post_id = p.id
                ) AS likes_count,
                EXISTS (
                    SELECT 1
                    FROM post_likes l
                    WHERE l.post_id = p.id AND l.user_id = :currentUserId
                ) AS is_liked,
                u.id AS autor_id,
                pf.username AS autor_username,
                pf.full_name AS autor_full_name,
                pf.avatar_url AS autor_avatar_url,
                p.image_url
            FROM posts p
            JOIN usuarios u ON u.id = p.autor_id
            LEFT JOIN perfiles pf ON pf.usuario_id = u.id
            """;

    public List<FeedPostView> obtenerFeedPrincipal(int limit, int offset) {
        String sql = FEED_SELECT_BASE_SQL + """
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        return fetchFeedPosts(sql, params);
    }

    public List<FeedPostView> obtenerPublicacionesPorUsuario(String username, int limit, int offset) {
        String sql = FEED_SELECT_BASE_SQL + """
                WHERE LOWER(COALESCE(pf.username, '')) = :username
                   OR LOWER(SPLIT_PART(COALESCE(u.email, ''), '@', 1)) = :username
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("limit", limit)
                .addValue("offset", offset);

        return fetchFeedPosts(sql, params);
    }

    private List<FeedPostView> fetchFeedPosts(String sql, MapSqlParameterSource params) {
        UUID currentUserId = getCurrentUserIdSafe();
        params.addValue("currentUserId", currentUserId);

        try {
            List<FeedPostView> posts = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = rawCreatedAt != null ? 
                    rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null;

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
                        createdAt,
                        rs.getInt("comments_count"),
                        rs.getInt("likes_count"),
                        rs.getBoolean("is_liked"),
                        autor,
                        new ArrayList<>(),
                        rs.getString("location"),
                        rs.getString("image_url"));
            });

            if (posts.isEmpty()) {
                return posts;
            }

            try {
                List<UUID> postIds = posts.stream().map(FeedPostView::id).toList();
                Map<UUID, List<String>> tagsByPost = obtenerTagsPorPostIds(postIds);

                return posts.stream()
                        .map(post -> new FeedPostView(
                                post.id(), post.titulo(), post.contenido(), post.isOfficial(),
                                post.createdAt(), post.commentsCount(), post.likesCount(), post.isLiked(),
                                post.autor(), 
                                tagsByPost.getOrDefault(post.id(), extractHashtags(post.titulo(), post.contenido())),
                                post.location(), post.imageUrl()))
                        .toList();
            } catch (Exception e) {
                return posts;
            }
        } catch (Exception e) {
            System.err.println("[FeedQueryService] ERROR SQL: " + e.getMessage());
            return new ArrayList<>();
        }
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

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("postId", postId);

        try {
            List<CommentThreadView> comentarios = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                return new CommentThreadView(
                        rs.getObject("id", UUID.class),
                        rs.getObject("post_id", UUID.class),
                        rs.getObject("parent_id", UUID.class),
                        rs.getObject("autor_id", UUID.class),
                        rs.getString("contenido"),
                        rawCreatedAt != null ? rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null);
            });

            Map<UUID, CommentThreadView> porId = new HashMap<>();
            for (CommentThreadView comentario : comentarios) {
                porId.put(comentario.id(), comentario);
            }

            List<CommentThreadView> raices = new ArrayList<>();
            for (CommentThreadView comentario : comentarios) {
                UUID parentId = comentario.parentId();
                if (parentId != null && porId.containsKey(parentId)) {
                    porId.get(parentId).respuestas().add(comentario);
                } else {
                    raices.add(comentario);
                }
            }

            return raices;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<TagSuggestionView> obtenerTagsDisponibles(String search, int limit) {
        String sql = """
                SELECT tag_name, usage_count 
                FROM (
                    SELECT LOWER(TRIM(tag)) AS tag_name, COUNT(*) as usage_count 
                    FROM post_tags 
                    GROUP BY tag_name
                ) combined 
                LIMIT :limit
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);
        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> 
                new TagSuggestionView(rs.getString("tag_name"), rs.getString("tag_name"), rs.getInt("usage_count")));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Map<UUID, List<String>> obtenerTagsPorPostIds(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();
        
        try {
            String sql = "SELECT post_id, LOWER(TRIM(tag)) AS tag FROM post_tags WHERE post_id IN (:postIds)";
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("postIds", postIds);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            Map<UUID, List<String>> result = new HashMap<>();
            for (Map<String, Object> row : rows) {
                UUID pId = (UUID) row.get("post_id");
                String tag = (String) row.get("tag");
                if (pId != null && tag != null) {
                    result.computeIfAbsent(pId, k -> new ArrayList<>()).add(tag);
                }
            }
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<String> extractHashtags(String title, String content) {
        String source = ((title == null ? "" : title) + " " + (content == null ? "" : content)).trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#[\\p{L}\\p{N}_]+").matcher(source);
        LinkedHashMap<String, Boolean> unique = new LinkedHashMap<>();
        while (matcher.find() && unique.size() < 5) {
            String tag = matcher.group().replaceFirst("^#", "").toLowerCase();
            if (!tag.isBlank()) unique.put(tag, Boolean.TRUE);
        }
        return new ArrayList<>(unique.keySet());
    }

    private UUID getCurrentUserIdSafe() {
        try {
            return securityService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
