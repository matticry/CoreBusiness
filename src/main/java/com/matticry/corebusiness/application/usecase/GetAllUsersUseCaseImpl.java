package com.matticry.corebusiness.application.usecase;

import com.matticry.corebusiness.domain.model.User;
import com.matticry.corebusiness.domain.port.in.GetAllUsersUseCase;
import com.matticry.corebusiness.domain.port.out.UserRepositoryPort;
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
