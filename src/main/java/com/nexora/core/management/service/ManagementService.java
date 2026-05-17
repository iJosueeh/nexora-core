package com.nexora.core.management.service;

import java.util.List;
import java.util.UUID;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.management.graphql.dto.AdminStatsView;
import com.nexora.core.graphql.dto.FeedPostView;

public interface ManagementService {
    AdminStatsView getAdminStats();
    List<ProfileView> getAllUsers(int limit, int offset, String search);
    ProfileView updateUserStatus(UUID userId, boolean isActive);
    FeedPostView markPostAsOfficial(UUID postId, boolean isOfficial);
    boolean deletePost(UUID postId);
}
