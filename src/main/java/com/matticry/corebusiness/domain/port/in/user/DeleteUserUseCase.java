package com.matticry.corebusiness.domain.port.in.user;

/**
 * Puerto de entrada para eliminar un usuario por su ID.
 */
public interface DeleteUserUseCase {

    void execute(Long id);
}
