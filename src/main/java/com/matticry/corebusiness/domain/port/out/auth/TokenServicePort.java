package com.matticry.corebusiness.domain.port.out.auth;

import java.time.OffsetDateTime;

/**
 * Puerto de salida para la generación y validación de tokens JWT y Refresh Tokens opacos.
 */
public interface TokenServicePort {

    String generateAccessToken(Long userId, String email);

    String generateRefreshToken();

    String hashToken(String token);

    OffsetDateTime getAccessTokenExpiration();

    OffsetDateTime getRefreshTokenExpiration();
}
