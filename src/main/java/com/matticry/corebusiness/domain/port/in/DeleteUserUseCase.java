package com.matticry.corebusiness.domain.port.in;

/**
 * Puerto de entrada para eliminar un usuario por su ID.
 */
public interface DeleteUserUseCase {

    void execute(Long id);
}
