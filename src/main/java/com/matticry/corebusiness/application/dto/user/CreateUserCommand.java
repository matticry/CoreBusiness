package com.matticry.corebusiness.application.dto.user;

/**
 * Comando interno con los datos necesarios para registrar un usuario.
 */
public record CreateUserCommand(
        String name,
        String lastname,
        String dni,
        String email,
        String password
) {
}
