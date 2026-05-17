package com.nexora.core.management.graphql.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentActivityView {
    private UUID id;
    private String type;
    private String description;
    private OffsetDateTime createdAt;
}
