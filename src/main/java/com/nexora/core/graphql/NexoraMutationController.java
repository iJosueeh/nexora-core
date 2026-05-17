package com.nexora.core.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.nexora.core.auth.services.AuthService;
import com.nexora.core.content.services.InteractionService;
import com.nexora.core.content.services.SocialService;
import com.nexora.core.graphql.dto.CreatePublicationInput;
import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.graphql.dto.UpdateProfileInput;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraMutationController {

    private final FeedMutationService feedMutationService;
    private final AuthService authService;
    private final InteractionService interactionService;
    private final SocialService socialService;

    @MutationMapping
    public FeedPostView crearPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument CreatePublicationInput input) {
        return feedMutationService.crearPublicacion(jwt, input);
    }

    @MutationMapping
    public ProfileView actualizarPerfil(@AuthenticationPrincipal Jwt jwt, @Argument UpdateProfileInput input) {
        String email = jwt.getClaimAsString("email");
        return authService.actualizarPerfil(email, input);
    }

    @MutationMapping
    public boolean toggleLike(@Argument UUID postId) {
        return interactionService.toggleLike(postId);
    }

    @MutationMapping
    public boolean toggleFollow(@Argument UUID targetUserId) {
        return socialService.toggleFollow(targetUserId);
    }
}
