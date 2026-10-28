package com.matticry.corebusiness.domain.port.in;

import com.matticry.corebusiness.domain.model.User;

/**
 * Puerto de entrada para consultar un usuario por su ID.
 */
public interface GetUserByIdUseCase {

    User execute(Long id);
}
