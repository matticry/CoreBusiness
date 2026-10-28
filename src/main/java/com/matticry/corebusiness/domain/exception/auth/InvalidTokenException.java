package com.matticry.corebusiness.domain.exception.auth;

/**
 * Excepción de dominio lanzada cuando un token JWT o Refresh Token es inválido, manipulado o expiró.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
