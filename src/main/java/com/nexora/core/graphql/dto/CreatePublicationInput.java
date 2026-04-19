package com.nexora.core.graphql.dto;

import java.util.List;

public record CreatePublicationInput(
        String titulo,
        String contenido,
        List<String> tags,
        String location) {
}
