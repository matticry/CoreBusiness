package com.matticry.corebusiness.domain.exception;

/**
 * Excepción de dominio lanzada cuando se intenta registrar o actualizar
 * un usuario con un correo o DNI que ya existe en el sistema.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
