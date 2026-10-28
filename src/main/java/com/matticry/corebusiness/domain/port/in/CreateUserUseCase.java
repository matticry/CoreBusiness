package com.matticry.corebusiness.domain.port.in;

import com.matticry.corebusiness.application.dto.CreateUserCommand;
import com.matticry.corebusiness.domain.model.User;

/**
 * Puerto de entrada para el caso de uso de creación de usuario.
 */
public interface CreateUserUseCase {

    User execute(CreateUserCommand command);
}
