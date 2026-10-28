package com.matticry.corebusiness.domain.port.in.auth;

import com.matticry.corebusiness.application.dto.auth.RefreshTokenCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;

/**
 * Puerto de entrada para el caso de uso de refresco de tokens (rotación).
 */
public interface RefreshTokenUseCase {

    TokenResult execute(RefreshTokenCommand command);
}
