package com.matticry.corebusiness.domain.port.in.user;

import com.matticry.corebusiness.application.dto.user.UpdateUserCommand;
import com.matticry.corebusiness.domain.model.user.User;

/**
 * Puerto de entrada para el caso de uso de actualización de usuario.
 */
public interface UpdateUserUseCase {

    User execute(Long id, UpdateUserCommand command);
}
