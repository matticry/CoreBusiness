package com.matticry.corebusiness.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * DTO de respuesta con el par de tokens emitidos.
 */
@Builder
@Schema(description = "Par de access token y refresh token")
public record TokenResponseDto(

        @Schema(description = "Access token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Fecha y hora de expiración del access token", example = "2026-10-28T16:45:00Z")
        OffsetDateTime accessTokenExpiresAt,

        @Schema(description = "Refresh token opaco de un solo uso", example = "q3J...")
        String refreshToken,

        @Schema(description = "Fecha y hora de expiración del refresh token", example = "2026-11-04T16:30:00Z")
        OffsetDateTime refreshTokenExpiresAt
) {
}
