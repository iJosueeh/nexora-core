package com.nexora.core.graphql.dto;

import java.util.List;
import java.util.UUID;

public record ProfileView(
        UUID id,
        String email,
        String username,
        String fullName,
        String bio,
        String career,
        String avatarUrl,
        String bannerUrl,
        Integer followersCount,
        Integer followingCount,
        List<String> academicInterests,
        Boolean profileComplete) {
}
