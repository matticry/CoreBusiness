package com.matticry.corebusiness.application.dto;

/**
 * Comando interno con los datos necesarios para actualizar un usuario.
 */
public record UpdateUserCommand(
        String name,
        String lastname,
        String dni,
        String email,
        String password
) {
}
