package com.matticry.corebusiness.infrastructure.adapter.persistence.mapper.auth;

import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.infrastructure.adapter.persistence.entity.auth.UserTokenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre la entidad JPA UserTokenEntity y el modelo de dominio UserToken.
 */
@Component
public class UserTokenEntityMapper {

    public UserToken toDomain(UserTokenEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .accessToken(entity.getAccessToken())
                .accessTokenExpiresAt(entity.getAccessTokenExpiresAt())
                .refreshToken(entity.getRefreshToken())
                .refreshTokenExpiresAt(entity.getRefreshTokenExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public UserTokenEntity toEntity(UserToken domain) {
        if (domain == null) {
            return null;
        }
        return UserTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .accessToken(domain.getAccessToken())
                .accessTokenExpiresAt(domain.getAccessTokenExpiresAt())
                .refreshToken(domain.getRefreshToken())
                .refreshTokenExpiresAt(domain.getRefreshTokenExpiresAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
