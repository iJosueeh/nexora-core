package com.nexora.core.auth.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private long expiresIn;
    
    private UserData user;

    @Data
    @Builder
    public static class UserData {
        private UUID id;
        private String email;
        private String username;
        
        @JsonProperty("profile_complete")
        private boolean profileComplete;
    }
}
