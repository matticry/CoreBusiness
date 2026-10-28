package com.matticry.corebusiness.application.usecase.user;

import com.matticry.corebusiness.application.dto.user.UpdateUserCommand;
import com.matticry.corebusiness.domain.exception.user.UserAlreadyExistsException;
import com.matticry.corebusiness.domain.exception.user.UserNotFoundException;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.user.UpdateUserUseCase;
import com.matticry.corebusiness.domain.port.out.user.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Caso de uso para actualizar un usuario existente.
 */
@Service
@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public User execute(Long id, UpdateUserCommand command) {
        User existingUser = userRepositoryPort.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));

        String normalizedEmail = command.email().trim().toLowerCase();
        String normalizedDni = command.dni().trim();

        if (userRepositoryPort.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new UserAlreadyExistsException("Ya existe otro usuario registrado con el email: " + normalizedEmail);
        }

        if (userRepositoryPort.existsByDniAndIdNot(normalizedDni, id)) {
            throw new UserAlreadyExistsException("Ya existe otro usuario registrado con el DNI: " + normalizedDni);
        }

        OffsetDateTime now = OffsetDateTime.now();

        existingUser.setName(command.name().trim());
        existingUser.setLastname(command.lastname().trim());
        existingUser.setDni(normalizedDni);
        existingUser.setEmail(normalizedEmail);
        existingUser.setUpdatedAt(now);

        if (command.password() != null && !command.password().isBlank()) {
            existingUser.setPassword(passwordEncoderPort.encode(command.password()));
            existingUser.setPasswordChangedAt(now);
        }

        return userRepositoryPort.save(existingUser);
    }
}
