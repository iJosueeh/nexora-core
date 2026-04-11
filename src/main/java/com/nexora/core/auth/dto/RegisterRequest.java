package com.nexora.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must have between 3 and 60 characters")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un formato de email válido")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@utp\\.edu\\.pe$", 
        message = "Solo se permiten correos institucionales de la UTP"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters")
    private String password;
}
