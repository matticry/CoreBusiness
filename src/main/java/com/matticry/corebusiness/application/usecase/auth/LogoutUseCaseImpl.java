package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.domain.port.in.auth.LogoutUseCase;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para el cierre de sesión mediante la revocación del refresh token.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final UserTokenRepositoryPort userTokenRepositoryPort;
    private final TokenServicePort tokenServicePort;

    @Override
    @Transactional
    public void execute(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            userTokenRepositoryPort.deleteByRefreshToken(tokenServicePort.hashToken(refreshToken.trim()));
        }
    }
}
