package com.matticry.corebusiness.domain.port.out.user;

import com.matticry.corebusiness.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para las operaciones de persistencia de usuarios.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);

    List<User> findAll();

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    void deleteById(Long id);
}
