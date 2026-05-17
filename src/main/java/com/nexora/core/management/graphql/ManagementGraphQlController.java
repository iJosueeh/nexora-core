package com.nexora.core.management.graphql;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.nexora.core.graphql.dto.FeedPostView;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.management.graphql.dto.AdminStatsView;
import com.nexora.core.management.service.ManagementService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ManagementGraphQlController {

    private final ManagementService managementService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public AdminStatsView adminStats() {
        return managementService.getAdminStats();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProfileView> allUsers(@Argument Integer limit, @Argument Integer offset, @Argument String search) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, offset);
        String safeSearch = search == null ? "" : search.trim();
        return managementService.getAllUsers(safeLimit, safeOffset, safeSearch);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProfileView updateUserStatus(@Argument UUID userId, @Argument Boolean isActive) {
        return managementService.updateUserStatus(userId, isActive);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public FeedPostView markPostAsOfficial(@Argument UUID postId, @Argument Boolean isOfficial) {
        return managementService.markPostAsOfficial(postId, isOfficial);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deletePost(@Argument UUID postId) {
        return managementService.deletePost(postId);
    }
}
