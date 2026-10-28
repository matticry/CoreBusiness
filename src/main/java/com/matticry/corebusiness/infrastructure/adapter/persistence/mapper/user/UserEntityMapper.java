package com.matticry.corebusiness.infrastructure.adapter.persistence.mapper.user;

import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.infrastructure.adapter.persistence.entity.user.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre la entidad JPA UserEntity y el modelo de dominio User.
 */
@Component
public class UserEntityMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastname(entity.getLastname())
                .dni(entity.getDni())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .passwordChangedAt(entity.getPasswordChangedAt())
                .emailVerified(entity.getEmailVerified())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return UserEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .lastname(domain.getLastname())
                .dni(domain.getDni())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .passwordChangedAt(domain.getPasswordChangedAt())
                .emailVerified(domain.getEmailVerified())
                .build();
    }
}
