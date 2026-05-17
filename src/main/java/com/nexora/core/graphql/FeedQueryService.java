package com.nexora.core.graphql;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.nexora.core.graphql.dto.CommentThreadView;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.TagSuggestionView;
import com.nexora.core.graphql.dto.TrendingTopicView;
import com.nexora.core.security.service.SecurityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SecurityService securityService;
    
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

                String autorUsername = Objects.requireNonNullElse(rs.getString("autor_username"), "");
                String autorFullName = Objects.requireNonNullElse(rs.getString("autor_full_name"), "");
                String autorAvatarUrl = Objects.requireNonNullElse(rs.getString("autor_avatar_url"), "");

                FeedAuthorView autor = new FeedAuthorView(
                    rs.getObject("autor_id", UUID.class),
                    autorUsername,
                    autorFullName,
                    autorAvatarUrl);

                String titulo = Objects.requireNonNullElse(rs.getString("titulo"), "");
                String contenido = Objects.requireNonNullElse(rs.getString("contenido"), "");
                String location = Objects.requireNonNullElse(rs.getString("location"), "");
                String imageUrl = Objects.requireNonNullElse(rs.getString("image_url"), "");

                return new FeedPostView(
                    rs.getObject("id", UUID.class),
                    titulo,
                    contenido,
                    rs.getBoolean("is_official"),
                    createdAt,
                    rs.getInt("comments_count"),
                    rs.getInt("likes_count"),
                    rs.getBoolean("is_liked"),
                    autor,
                    new ArrayList<>(),
                    location,
                    imageUrl);
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
                log.warn("[FeedQueryService] Could not enrich posts with tags; returning posts without tags: {}", e.getMessage(), e);
                return posts;
            }
        } catch (Exception e) {
            log.error("[FeedQueryService] ERROR SQL: {}", e.getMessage(), e);
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
        // Calcular timestamp de corte (últimas 24 horas) y pasarlo como parámetro
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("limit", limit)
            .addValue("since", Timestamp.valueOf(since));
        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                String tagName = Objects.requireNonNullElse(rs.getString("tag_name"), "");
                return new TagSuggestionView(tagName, tagName, rs.getInt("usage_count"));
            });
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

    public List<TrendingTopicView> obtenerTrendingTopics(int limit) {
        // SQL usando agregados por post (LEFT JOIN a subselects) y parámetros nombrados
        String sql = """
                SELECT
                    p.id,
                    p.titulo,
                    p.content AS contenido,
                    p.location,
                    COALESCE(p.is_official, FALSE) AS is_official,
                    p.created_at,
                    COALESCE(c.comments_count_24h, 0) AS comments_count_24h,
                    COALESCE(l.likes_count_24h, 0) AS likes_count_24h,
                    u.id AS autor_id,
                    pf.username AS autor_username,
                    pf.full_name AS autor_full_name,
                    pf.avatar_url AS autor_avatar_url,
                    p.image_url
                FROM posts p
                LEFT JOIN (
                    SELECT post_id, COUNT(*) AS comments_count_24h
                    FROM comentarios
                    WHERE created_at >= :since
                    GROUP BY post_id
                ) c ON c.post_id = p.id
                LEFT JOIN (
                    SELECT post_id, COUNT(*) AS likes_count_24h
                    FROM post_likes
                    WHERE created_at >= :since
                    GROUP BY post_id
                ) l ON l.post_id = p.id
                JOIN usuarios u ON u.id = p.autor_id
                LEFT JOIN perfiles pf ON pf.usuario_id = u.id
                WHERE p.created_at >= :since
                ORDER BY (COALESCE(c.comments_count_24h,0) + COALESCE(l.likes_count_24h,0)) DESC, p.created_at DESC
                LIMIT :limit
                """;

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("since", Timestamp.valueOf(since));

        try {
            List<TrendingTopicView> topics = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Timestamp rawCreatedAt = rs.getTimestamp("created_at");
                OffsetDateTime createdAt = rawCreatedAt != null ? 
                    rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC) : null;

                int commentsCount = rs.getInt("comments_count_24h");
                int likesCount = rs.getInt("likes_count_24h");
                int interactionScore = commentsCount + likesCount;

                String autorUsername = Objects.requireNonNullElse(rs.getString("autor_username"), "");
                String autorFullName = Objects.requireNonNullElse(rs.getString("autor_full_name"), "");
                String autorAvatarUrl = Objects.requireNonNullElse(rs.getString("autor_avatar_url"), "");

                FeedAuthorView autor = new FeedAuthorView(
                    rs.getObject("autor_id", UUID.class),
                    autorUsername,
                    autorFullName,
                    autorAvatarUrl);

                String titulo = Objects.requireNonNullElse(rs.getString("titulo"), "");
                String contenido = Objects.requireNonNullElse(rs.getString("contenido"), "");
                String location = Objects.requireNonNullElse(rs.getString("location"), "");
                String imageUrl = Objects.requireNonNullElse(rs.getString("image_url"), "");

                return new TrendingTopicView(
                    rs.getObject("id", UUID.class),
                    titulo,
                    contenido,
                    rs.getBoolean("is_official"),
                    createdAt,
                    commentsCount,
                    likesCount,
                    interactionScore,
                    autor,
                    new ArrayList<>(),
                    location,
                    imageUrl);
            });

            if (topics.isEmpty()) {
                return topics;
            }

            try {
                List<UUID> postIds = topics.stream().map(TrendingTopicView::id).toList();
                Map<UUID, List<String>> tagsByPost = obtenerTagsPorPostIds(postIds);

                return topics.stream()
                        .map(topic -> new TrendingTopicView(
                                topic.id(), topic.titulo(), topic.contenido(), topic.isOfficial(),
                                topic.createdAt(), topic.commentsCount(), topic.likesCount(), topic.interactionScore(),
                                topic.autor(), 
                                tagsByPost.getOrDefault(topic.id(), extractHashtags(topic.titulo(), topic.contenido())),
                                topic.location(), topic.imageUrl()))
                        .toList();
            } catch (Exception e) {
                log.warn("[FeedQueryService] Could not enrich topics with tags; returning topics without tags: {}", e.getMessage(), e);
                return topics;
            }
        } catch (Exception e) {
            log.error("[FeedQueryService] ERROR obtaining trending topics: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private UUID getCurrentUserIdSafe() {
        try {
            return securityService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
