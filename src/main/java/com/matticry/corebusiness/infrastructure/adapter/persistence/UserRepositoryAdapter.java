package com.matticry.corebusiness.infrastructure.adapter.persistence;

import com.matticry.corebusiness.domain.model.User;
import com.matticry.corebusiness.domain.port.out.UserRepositoryPort;
import com.matticry.corebusiness.infrastructure.adapter.persistence.entity.UserEntity;
import com.matticry.corebusiness.infrastructure.adapter.persistence.mapper.UserEntityMapper;
import com.matticry.corebusiness.infrastructure.adapter.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto UserRepositoryPort
 * interactuando con UserJpaRepository y mapeando con UserEntityMapper.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public User save(User user) {
        UserEntity entity = userEntityMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByDni(String dni) {
        return userJpaRepository.findByDni(dni)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(userEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByDni(String dni) {
        return userJpaRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userJpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsByDniAndIdNot(String dni, Long id) {
        return userJpaRepository.existsByDniAndIdNot(dni, id);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}
