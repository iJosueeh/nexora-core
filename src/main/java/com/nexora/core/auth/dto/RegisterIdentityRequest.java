package com.nexora.core.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterIdentityRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must have between 3 and 60 characters")
    private String username;

    @NotBlank(message = "Full name is required")
    @Size(min = 1, max = 100, message = "Full name must have between 1 and 100 characters")
    private String fullName;

    @NotBlank(message="Biography is required")
    @Size(min = 1, max = 255, message ="Biography must have between 1 and 255 characters" )
    private String bio;
}
