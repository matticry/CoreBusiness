package com.matticry.corebusiness.application.dto.auth;

/**
 * Comando interno con el refresh token para solicitar un nuevo par de tokens.
 */
public record RefreshTokenCommand(
        String refreshToken
) {
}
