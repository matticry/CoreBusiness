package com.matticry.corebusiness.infrastructure.adapter.persistence.repository.auth;

import com.matticry.corebusiness.infrastructure.adapter.persistence.entity.auth.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad UserTokenEntity.
 */
@Repository
public interface UserTokenJpaRepository extends JpaRepository<UserTokenEntity, Long> {

    Optional<UserTokenEntity> findByRefreshToken(String refreshToken);

    Optional<UserTokenEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByRefreshToken(String refreshToken);
}
