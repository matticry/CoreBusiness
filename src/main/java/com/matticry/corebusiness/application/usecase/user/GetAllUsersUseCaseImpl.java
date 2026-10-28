package com.matticry.corebusiness.application.usecase.user;

import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.user.GetAllUsersUseCase;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso para obtener todos los usuarios registrados.
 */
@Service
@RequiredArgsConstructor
public class GetAllUsersUseCaseImpl implements GetAllUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<User> execute() {
        return userRepositoryPort.findAll();
    }
}
