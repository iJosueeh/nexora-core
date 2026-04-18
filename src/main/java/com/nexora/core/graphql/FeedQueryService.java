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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final List<String> TAG_COLUMN_CANDIDATES = List.of("tag", "tag_name", "tags", "name");

    public List<FeedPostView> obtenerFeedPrincipal(int limit, int offset) {
        String sql = """
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
                    u.id AS autor_id,
                    pf.username AS autor_username,
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

        List<FeedPostView> posts = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Timestamp rawCreatedAt = rs.getTimestamp("created_at");
            OffsetDateTime createdAt = null;
            if (rawCreatedAt != null) {
                createdAt = rawCreatedAt.toLocalDateTime().atOffset(ZoneOffset.UTC);
            }

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
                    autor,
                    List.of(),
                    rs.getString("location"));
        });

        if (posts.isEmpty()) {
            return posts;
        }

        List<UUID> postIds = posts.stream().map(FeedPostView::id).toList();
        Map<UUID, List<String>> tagsByPost = obtenerTagsPorPostIds(postIds);

        return posts.stream()
                .map(post -> new FeedPostView(
                        post.id(),
                        post.titulo(),
                        post.contenido(),
                        post.isOfficial(),
                        post.createdAt(),
                        post.commentsCount(),
                        post.autor(),
                        tagsByPost.getOrDefault(post.id(), extractHashtags(post.titulo(), post.contenido())),
                        post.location()))
                .toList();
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
            OffsetDateTime createdAt = null;
            if (rawCreatedAt != null) {
                createdAt = rawCreatedAt.toInstant().atOffset(ZoneOffset.UTC);
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

    public List<TagSuggestionView> obtenerTagsDisponibles(String search, int limit) {
        String sql = """
                WITH explicit_tags AS (
                    SELECT LOWER(TRIM(pt.tag)) AS tag_name
                    FROM post_tags pt
                    WHERE pt.tag IS NOT NULL AND TRIM(pt.tag) <> ''
                ),
                extracted_tags AS (
                    SELECT LOWER(REGEXP_REPLACE(match[1], '^#', '')) AS tag_name
                    FROM posts p
                    CROSS JOIN LATERAL regexp_matches(
                        COALESCE(p.titulo, '') || ' ' || COALESCE(p.content, ''),
                        '#[[:alnum:]_]+',
                        'g'
                    ) AS match
                    ),
                    combined_tags AS (
                        SELECT tag_name FROM explicit_tags
                        UNION ALL
                        SELECT tag_name FROM extracted_tags
                )
                SELECT
                    tag_name,
                    COUNT(*) AS usage_count
                    FROM combined_tags
                WHERE (:search = '' OR tag_name LIKE CONCAT('%', :search, '%'))
                GROUP BY tag_name
                ORDER BY usage_count DESC, tag_name ASC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", search == null ? "" : search)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String tagName = rs.getString("tag_name");
            return new TagSuggestionView(
                    tagName,
                    tagName,
                    rs.getInt("usage_count"));
        });
    }

    private Map<UUID, List<String>> obtenerTagsPorPostIds(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

                String tagColumn = resolveTagColumn();
                if (tagColumn == null) {
                        return Map.of();
                }

        String sql = """
                                SELECT pt.post_id, LOWER(TRIM(pt.%s)) AS tag
                FROM post_tags pt
                WHERE pt.post_id IN (:postIds)
                                    AND pt.%s IS NOT NULL
                                    AND TRIM(pt.%s) <> ''
                                ORDER BY pt.post_id, pt.%s
                                """.formatted(tagColumn, tagColumn, tagColumn, tagColumn);

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("postIds", postIds);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        Map<UUID, List<String>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            UUID postId = (UUID) row.get("post_id");
            String tag = (String) row.get("tag");
            if (postId == null || tag == null || tag.isBlank()) {
                continue;
            }
            result.computeIfAbsent(postId, ignored -> new ArrayList<>()).add(tag);
        }

        return result;
    }

    private String resolveTagColumn() {
        for (String candidate : TAG_COLUMN_CANDIDATES) {
            Boolean exists = jdbcTemplate.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1
                        FROM information_schema.columns
                        WHERE table_name = 'post_tags'
                          AND column_name = :columnName
                    )
                    """, new MapSqlParameterSource("columnName", candidate), Boolean.class);

            if (Boolean.TRUE.equals(exists)) {
                return candidate;
            }
        }

        return null;
    }

    private List<String> extractHashtags(String title, String content) {
        String source = ((title == null ? "" : title) + " " + (content == null ? "" : content)).trim();
        if (source.isEmpty()) {
            return List.of();
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#[\\p{L}\\p{N}_]+", java.util.regex.Pattern.UNICODE_CHARACTER_CLASS)
                .matcher(source);
        LinkedHashMap<String, Boolean> unique = new LinkedHashMap<>();
        while (matcher.find() && unique.size() < 5) {
            String raw = matcher.group();
            String normalized = raw.replaceFirst("^#", "").toLowerCase();
            if (!normalized.isBlank()) {
                unique.put(normalized, Boolean.TRUE);
            }
        }

        return new ArrayList<>(unique.keySet());
    }
}
