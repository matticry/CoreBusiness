package com.matticry.corebusiness.domain.exception.auth;

/**
 * Excepción de dominio lanzada cuando las credenciales proporcionadas son incorrectas o inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Credenciales de autenticación inválidas.");
    }
}
