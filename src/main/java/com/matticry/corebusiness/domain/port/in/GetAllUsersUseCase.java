package com.matticry.corebusiness.domain.port.in;

import com.matticry.corebusiness.domain.model.User;

import java.util.List;

/**
 * Puerto de entrada para listar todos los usuarios.
 */
public interface GetAllUsersUseCase {

    List<User> execute();
}
