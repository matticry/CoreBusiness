package com.matticry.corebusiness.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entidad de dominio pura que representa los tokens de sesión de un usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {

    private Long id;
    private Long userId;
    private String accessToken;
    private OffsetDateTime accessTokenExpiresAt;
    private String refreshToken;
    private OffsetDateTime refreshTokenExpiresAt;
    private OffsetDateTime createdAt;

    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt == null || OffsetDateTime.now().isAfter(accessTokenExpiresAt);
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt == null || OffsetDateTime.now().isAfter(refreshTokenExpiresAt);
    }
}
