package com.matticry.corebusiness.infrastructure.adapter.persistence.auth;

import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import com.matticry.corebusiness.infrastructure.adapter.persistence.entity.auth.UserTokenEntity;
import com.matticry.corebusiness.infrastructure.adapter.persistence.mapper.auth.UserTokenEntityMapper;
import com.matticry.corebusiness.infrastructure.adapter.persistence.repository.auth.UserTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa UserTokenRepositoryPort.
 */
@Repository
@RequiredArgsConstructor
public class UserTokenRepositoryAdapter implements UserTokenRepositoryPort {

    private final UserTokenJpaRepository userTokenJpaRepository;
    private final UserTokenEntityMapper userTokenEntityMapper;

    @Override
    public UserToken save(UserToken userToken) {
        UserTokenEntity entity = userTokenEntityMapper.toEntity(userToken);
        UserTokenEntity saved = userTokenJpaRepository.save(entity);
        return userTokenEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<UserToken> findByRefreshToken(String refreshToken) {
        return userTokenJpaRepository.findByRefreshToken(refreshToken)
                .map(userTokenEntityMapper::toDomain);
    }

    @Override
    public Optional<UserToken> findByUserId(Long userId) {
        return userTokenJpaRepository.findByUserId(userId)
                .map(userTokenEntityMapper::toDomain);
    }

    @Override
    public void deleteByUserId(Long userId) {
        userTokenJpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        userTokenJpaRepository.deleteByRefreshToken(refreshToken);
    }
}
