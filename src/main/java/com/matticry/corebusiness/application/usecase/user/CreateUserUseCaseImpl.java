package com.matticry.corebusiness.application.usecase.user;

import com.matticry.corebusiness.application.dto.user.CreateUserCommand;
import com.matticry.corebusiness.domain.exception.user.UserAlreadyExistsException;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.user.CreateUserUseCase;
import com.matticry.corebusiness.domain.port.out.user.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Caso de uso para crear un nuevo usuario con validaciones de unicidad y contraseña hasheada.
 */
@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public User execute(CreateUserCommand command) {
        String normalizedEmail = command.email().trim().toLowerCase();
        String normalizedDni = command.dni().trim();

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el email: " + normalizedEmail);
        }

        if (userRepositoryPort.existsByDni(normalizedDni)) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el DNI: " + normalizedDni);
        }

        OffsetDateTime now = OffsetDateTime.now();
        String hashedPassword = passwordEncoderPort.encode(command.password());

        User user = User.builder()
                .name(command.name().trim())
                .lastname(command.lastname().trim())
                .dni(normalizedDni)
                .email(normalizedEmail)
                .password(hashedPassword)
                .status(true)
                .emailVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .passwordChangedAt(null)
                .build();

        return userRepositoryPort.save(user);
    }
}
