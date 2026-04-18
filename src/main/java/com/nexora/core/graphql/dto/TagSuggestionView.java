package com.nexora.core.graphql.dto;

public record TagSuggestionView(
        String id,
        String name,
        int usageCount) {
}
