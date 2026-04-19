package com.nexora.core.graphql;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.content.entity.Post;
import com.nexora.core.content.repository.PostRepository;
import com.nexora.core.graphql.dto.CreatePublicationInput;
import com.nexora.core.graphql.dto.FeedAuthorView;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedMutationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProfilesRepository profilesRepository;

    @Transactional
    public FeedPostView crearPublicacion(Jwt jwt, CreatePublicationInput input) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        String content = input.contenido() == null ? "" : input.contenido().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Publication content is required");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Post post = new Post();
        post.setAutor(user);
        post.setTitulo(resolveTitle(input.titulo(), content));
        post.setContent(content);
        post.setLocation(resolveLocation(input.location()));
        post.setTags(resolveTags(input.tags()));
        post.setIsOfficial(false);
        post.setStatus("PUBLISHED");

        Post savedPost = postRepository.saveAndFlush(post);
        return toView(savedPost, resolveProfile(user));
    }

    private FeedPostView toView(Post post, Profiles profile) {
        String username = profile != null ? profile.getUsername() : post.getAutor().getEmail().split("@")[0];
        String fullName = profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()
                ? profile.getFullName()
                : username;
        String avatarUrl = profile != null && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()
                ? profile.getAvatarUrl()
                : null;

        FeedAuthorView autor = new FeedAuthorView(
                post.getAutor().getId(),
                username,
                fullName,
                avatarUrl);

        OffsetDateTime createdAt = post.getCreatedAt() == null
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : post.getCreatedAt().atOffset(ZoneOffset.UTC);

        return new FeedPostView(
                post.getId(),
                post.getTitulo(),
                post.getContent(),
                Boolean.TRUE.equals(post.getIsOfficial()),
                createdAt,
                0,
            autor,
            post.getTags() == null ? List.of() : List.copyOf(post.getTags()),
            post.getLocation());
    }

    private Profiles resolveProfile(User user) {
        return profilesRepository.findByUser_Id(user.getId());
    }

    private String resolveTitle(String title, String content) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        String firstLine = content.split("\\n")[0].trim();
        return firstLine.length() > 90 ? firstLine.substring(0, 90) : firstLine;
    }

    private String resolveLocation(String location) {
        if (location == null) {
            return null;
        }

        String trimmed = location.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }

    private List<String> resolveTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) {
                continue;
            }

            String tag = raw.replaceFirst("^#", "").trim().toLowerCase(Locale.ROOT);
            if (!tag.isEmpty()) {
                normalized.add(tag);
            }

            if (normalized.size() >= 8) {
                break;
            }
        }

        return new ArrayList<>(normalized);
    }
}
