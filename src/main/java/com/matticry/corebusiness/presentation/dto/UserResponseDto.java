package com.matticry.corebusiness.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * DTO de respuesta para la exposición de datos de un usuario.
 * Por motivos de seguridad, la contraseña nunca se expone.
 */
@Builder
@Schema(description = "Respuesta con los datos públicos del usuario")
public record UserResponseDto(

        @Schema(description = "Identificador único generado por la base de datos", example = "1")
        Long id,

        @Schema(description = "Nombre(s) del usuario", example = "Carlos")
        String name,

        @Schema(description = "Apellido(s) del usuario", example = "Gómez")
        String lastname,

        @Schema(description = "Documento Nacional de Identidad", example = "71829304")
        String dni,

        @Schema(description = "Correo electrónico del usuario", example = "carlos.gomez@empresa.com")
        String email,

        @Schema(description = "Estado de la cuenta del usuario (activo/inactivo)", example = "true")
        Boolean status,

        @Schema(description = "Fecha y hora de creación del registro", example = "2026-10-28T16:30:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "Fecha y hora de la última modificación del registro", example = "2026-10-28T16:30:00Z")
        OffsetDateTime updatedAt,

        @Schema(description = "Fecha y hora del último cambio de contraseña", example = "2026-10-28T16:30:00Z")
        OffsetDateTime passwordChangedAt,

        @Schema(description = "Indica si el correo electrónico ha sido verificado", example = "false")
        Boolean emailVerified
) {
}
