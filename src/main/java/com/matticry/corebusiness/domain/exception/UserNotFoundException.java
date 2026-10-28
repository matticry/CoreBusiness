package com.matticry.corebusiness.domain.exception;

/**
 * Excepción de dominio lanzada cuando un usuario no es encontrado.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException withId(Long id) {
        return new UserNotFoundException("Usuario no encontrado con ID: " + id);
    }
}
