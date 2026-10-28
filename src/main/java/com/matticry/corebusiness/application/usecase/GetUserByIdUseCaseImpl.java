package com.matticry.corebusiness.application.usecase;

import com.matticry.corebusiness.domain.exception.UserNotFoundException;
import com.matticry.corebusiness.domain.model.User;
import com.matticry.corebusiness.domain.port.in.GetUserByIdUseCase;
import com.matticry.corebusiness.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para obtener un usuario por su identificador único.
 */
@Service
@RequiredArgsConstructor
public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public User execute(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> UserNotFoundException.withId(id));
    }
}
