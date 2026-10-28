package com.matticry.corebusiness.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada con el refresh token para renovar o revocar la sesión.
 */
@Schema(description = "Refresh token emitido al iniciar sesión")
public record RefreshTokenRequestDto(

        @Schema(description = "Refresh token opaco", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
