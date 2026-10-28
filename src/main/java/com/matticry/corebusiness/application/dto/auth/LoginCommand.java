package com.matticry.corebusiness.application.dto.auth;

/**
 * Comando interno con las credenciales para iniciar sesión.
 */
public record LoginCommand(
        String email,
        String password
) {
}
