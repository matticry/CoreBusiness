package com.matticry.corebusiness.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada con las credenciales de inicio de sesión.
 */
@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequestDto(

        @Schema(description = "Correo electrónico registrado", example = "carlos.gomez@empresa.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @Schema(description = "Contraseña del usuario", example = "ClaveSegura123*")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
