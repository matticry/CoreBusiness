package com.matticry.corebusiness.domain.port.in.auth;

/**
 * Puerto de entrada para el caso de uso de cierre de sesión (revocación de tokens).
 */
public interface LogoutUseCase {

    void execute(String refreshToken);
}
