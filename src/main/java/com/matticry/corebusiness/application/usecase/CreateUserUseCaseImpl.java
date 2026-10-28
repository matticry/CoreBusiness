package com.matticry.corebusiness.application.usecase;

import com.matticry.corebusiness.application.dto.CreateUserCommand;
import com.matticry.corebusiness.domain.exception.UserAlreadyExistsException;
import com.matticry.corebusiness.domain.model.User;
import com.matticry.corebusiness.domain.port.in.CreateUserUseCase;
import com.matticry.corebusiness.domain.port.out.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.UserRepositoryPort;
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
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el email: " + command.email());
        }

        if (userRepositoryPort.existsByDni(command.dni())) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el DNI: " + command.dni());
        }

        OffsetDateTime now = OffsetDateTime.now();
        String hashedPassword = passwordEncoderPort.encode(command.password());

        User user = User.builder()
                .name(command.name().trim())
                .lastname(command.lastname().trim())
                .dni(command.dni().trim())
                .email(command.email().trim().toLowerCase())
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
