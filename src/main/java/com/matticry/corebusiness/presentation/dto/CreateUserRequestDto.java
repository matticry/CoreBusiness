package com.matticry.corebusiness.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de creación de usuario.
 */
@Schema(description = "Datos requeridos para el registro de un nuevo usuario")
public record CreateUserRequestDto(

        @Schema(description = "Nombre(s) del usuario", example = "Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String name,

        @Schema(description = "Apellido(s) del usuario", example = "Gómez", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastname,

        @Schema(description = "Documento Nacional de Identidad o documento identificativo", example = "71829304", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El DNI es obligatorio")
        @Size(min = 8, max = 20, message = "El DNI debe tener entre 8 y 20 caracteres")
        String dni,

        @Schema(description = "Correo electrónico institucional o personal", example = "carlos.gomez@empresa.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
        String email,

        @Schema(description = "Contraseña en texto plano que será hasheada", example = "ClaveSegura2026*", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {
}
