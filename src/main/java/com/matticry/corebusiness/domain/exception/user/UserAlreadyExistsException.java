package com.matticry.corebusiness.domain.exception.user;

/**
 * Excepción de dominio lanzada cuando se intenta registrar un usuario con un email o DNI ya existente.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
