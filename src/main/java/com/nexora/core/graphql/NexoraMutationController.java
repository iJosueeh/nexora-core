package com.nexora.core.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.nexora.core.graphql.dto.CreatePublicationInput;
import com.nexora.core.graphql.dto.FeedPostView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NexoraMutationController {

    private final FeedMutationService feedMutationService;

    @MutationMapping
    public FeedPostView crearPublicacion(@AuthenticationPrincipal Jwt jwt, @Argument CreatePublicationInput input) {
        return feedMutationService.crearPublicacion(jwt, input);
    }
}
