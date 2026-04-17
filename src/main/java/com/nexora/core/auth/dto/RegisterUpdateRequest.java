package com.nexora.core.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUpdateRequest {
    // Step 2 Fields
    @Size(min = 3, max = 60)
    private String username;
    
    private String fullName;
    
    @Size(max = 255)
    private String bio;

    // Step 3 Fields
    private String[] academicInterests;
}
