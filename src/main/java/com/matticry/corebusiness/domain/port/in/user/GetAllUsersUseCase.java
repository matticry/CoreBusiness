package com.matticry.corebusiness.domain.port.in.user;

import com.matticry.corebusiness.domain.model.user.User;

import java.util.List;

/**
 * Puerto de entrada para listar todos los usuarios.
 */
public interface GetAllUsersUseCase {

    List<User> execute();
}
