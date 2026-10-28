package com.matticry.corebusiness.domain.port.in;

import com.matticry.corebusiness.application.dto.UpdateUserCommand;
import com.matticry.corebusiness.domain.model.User;

/**
 * Puerto de entrada para el caso de uso de actualización de usuario.
 */
public interface UpdateUserUseCase {

    User execute(Long id, UpdateUserCommand command);
}
