package com.nexora.core.graphql.dto;

import java.util.List;

public record UpdateProfileInput(
        String username,
        String fullName,
        String bio,
        String career,
        String avatarUrl,
        String bannerUrl,
        List<String> academicInterests) {
}
