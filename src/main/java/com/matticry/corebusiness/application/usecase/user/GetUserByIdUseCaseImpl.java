package com.matticry.corebusiness.application.usecase.user;

import com.matticry.corebusiness.domain.exception.user.UserNotFoundException;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.user.GetUserByIdUseCase;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para consultar un usuario por su ID.
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
