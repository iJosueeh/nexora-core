package com.nexora.core.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterPreferencesRequest {
    @NotEmpty(message = "Academic interests are required")
    private String[] academicInterests;
}
