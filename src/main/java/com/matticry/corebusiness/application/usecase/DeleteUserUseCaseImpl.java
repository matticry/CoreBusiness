package com.matticry.corebusiness.application.usecase;

import com.matticry.corebusiness.domain.exception.UserNotFoundException;
import com.matticry.corebusiness.domain.port.in.DeleteUserUseCase;
import com.matticry.corebusiness.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para eliminar un usuario por su ID.
 */
@Service
@RequiredArgsConstructor
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional
    public void execute(Long id) {
        if (!userRepositoryPort.existsById(id)) {
            throw UserNotFoundException.withId(id);
        }
        userRepositoryPort.deleteById(id);
    }
}
