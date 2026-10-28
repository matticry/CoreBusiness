package com.matticry.corebusiness.domain.port.in.user;

import com.matticry.corebusiness.application.dto.user.CreateUserCommand;
import com.matticry.corebusiness.domain.model.user.User;

/**
 * Puerto de entrada para el caso de uso de creación de usuario.
 */
public interface CreateUserUseCase {

    User execute(CreateUserCommand command);
}
