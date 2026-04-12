package com.nexora.core.auth.dto;

import com.nexora.core.profile.entity.AcademicInterests;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {


    //Cuenta
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

    //Perfil
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must have between 3 and 60 characters")
    private String username;

    @NotBlank(message = "Full name is required")
    @Size(min = 1, max = 100, message = "Full name must have between 1 and 100 characters")
    private String fullName;

    @NotBlank(message="Biography is required")
    @Size(min = 1, max = 100,message ="Biography must have between 10 and 100 characters" )
    private String bio;


    //Preferencias
    @NotEmpty(message = "Academic interests are required")
    private String[] academicInterests;
}
