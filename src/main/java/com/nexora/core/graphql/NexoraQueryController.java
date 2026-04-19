package com.nexora.core.graphql;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.nexora.core.graphql.dto.CommentThreadView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.TagSuggestionView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraQueryController {

    private static final int MAX_OFFSET = 10_000;
    private static final int MAX_TAG_LIMIT = 30;

    private final FeedQueryService feedQueryService;

    @QueryMapping
    public String health() {
        return "Nexora GraphQL API is running";
    }

    @QueryMapping
    public List<FeedPostView> obtenerFeedPrincipal(@Argument Integer limit, @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, Math.min(offset, MAX_OFFSET));
        return feedQueryService.obtenerFeedPrincipal(safeLimit, safeOffset);
    }

    @QueryMapping
    public List<FeedPostView> publicacionesPorUsuario(@Argument String username, @Argument Integer limit,
            @Argument Integer offset) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, Math.min(offset, MAX_OFFSET));
        String safeUsername = username == null ? "" : username.trim().toLowerCase();

        if (safeUsername.isBlank()) {
            return List.of();
        }

        return feedQueryService.obtenerPublicacionesPorUsuario(safeUsername, safeLimit, safeOffset);
    }

    @QueryMapping
    public List<CommentThreadView> comentariosPorPost(@Argument UUID postId) {
        return feedQueryService.obtenerHilosComentarios(postId);
    }

    @QueryMapping
    public List<TagSuggestionView> availableTags(@Argument String search, @Argument Integer limit) {
        int safeLimit = limit == null ? 12 : Math.max(1, Math.min(limit, MAX_TAG_LIMIT));
        String safeSearch = search == null ? "" : search.trim().toLowerCase();
        return feedQueryService.obtenerTagsDisponibles(safeSearch, safeLimit);
    }
}
