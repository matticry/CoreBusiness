package com.matticry.corebusiness.domain.port.out.auth;

import com.matticry.corebusiness.domain.model.auth.UserToken;

import java.util.Optional;

/**
 * Puerto de salida para las operaciones de persistencia de tokens de usuario.
 */
public interface UserTokenRepositoryPort {

    UserToken save(UserToken userToken);

    Optional<UserToken> findByRefreshToken(String refreshToken);

    Optional<UserToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByRefreshToken(String refreshToken);
}
