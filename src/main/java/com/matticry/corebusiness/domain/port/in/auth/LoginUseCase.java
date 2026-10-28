package com.matticry.corebusiness.domain.port.in.auth;

import com.matticry.corebusiness.application.dto.auth.LoginCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;

/**
 * Puerto de entrada para el caso de uso de inicio de sesión.
 */
public interface LoginUseCase {

    TokenResult execute(LoginCommand command);
}
