package com.matticry.corebusiness.application.dto.auth;

import java.time.OffsetDateTime;

/**
 * Resultado de una operación de autenticación o refresco de tokens.
 */
public record TokenResult(
        String accessToken,
        OffsetDateTime accessTokenExpiresAt,
        String refreshToken,
        OffsetDateTime refreshTokenExpiresAt
) {
}
