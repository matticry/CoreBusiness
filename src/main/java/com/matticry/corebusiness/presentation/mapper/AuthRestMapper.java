package com.matticry.corebusiness.presentation.mapper;

import com.matticry.corebusiness.application.dto.auth.LoginCommand;
import com.matticry.corebusiness.application.dto.auth.RefreshTokenCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.presentation.dto.auth.LoginRequestDto;
import com.matticry.corebusiness.presentation.dto.auth.RefreshTokenRequestDto;
import com.matticry.corebusiness.presentation.dto.auth.TokenResponseDto;
import org.springframework.stereotype.Component;

/**
 * Mapper entre los DTOs HTTP de autenticación y los comandos/resultados de la capa de aplicación.
 */
@Component
public class AuthRestMapper {

    public LoginCommand toCommand(LoginRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new LoginCommand(dto.email(), dto.password());
    }

    public RefreshTokenCommand toCommand(RefreshTokenRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new RefreshTokenCommand(dto.refreshToken());
    }

    public TokenResponseDto toResponse(TokenResult result) {
        if (result == null) {
            return null;
        }
        return TokenResponseDto.builder()
                .accessToken(result.accessToken())
                .accessTokenExpiresAt(result.accessTokenExpiresAt())
                .refreshToken(result.refreshToken())
                .refreshTokenExpiresAt(result.refreshTokenExpiresAt())
                .build();
    }
}
