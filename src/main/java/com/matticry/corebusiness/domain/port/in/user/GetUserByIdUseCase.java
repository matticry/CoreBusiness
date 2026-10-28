package com.matticry.corebusiness.domain.port.in.user;

import com.matticry.corebusiness.domain.model.user.User;

/**
 * Puerto de entrada para consultar un usuario por su ID.
 */
public interface GetUserByIdUseCase {

    User execute(Long id);
}
