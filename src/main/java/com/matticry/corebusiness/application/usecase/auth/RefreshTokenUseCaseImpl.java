package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.application.dto.auth.RefreshTokenCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.domain.exception.auth.InvalidTokenException;
import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.auth.RefreshTokenUseCase;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Caso de uso para refrescar el access token rotando el refresh token según buenas prácticas.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserTokenRepositoryPort userTokenRepositoryPort;
    private final TokenServicePort tokenServicePort;

    @Override
    @Transactional
    public TokenResult execute(RefreshTokenCommand command) {
        String tokenHash = tokenServicePort.hashToken(command.refreshToken().trim());

        UserToken currentToken = userTokenRepositoryPort.findByRefreshToken(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido o revocado."));

        if (currentToken.isRefreshTokenExpired()) {
            userTokenRepositoryPort.deleteByRefreshToken(tokenHash);
            throw new InvalidTokenException("El refresh token ha expirado. Debe autenticarse nuevamente.");
        }

        User user = userRepositoryPort.findById(currentToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("El usuario asociado al token no existe."));

        if (Boolean.FALSE.equals(user.getStatus())) {
            userTokenRepositoryPort.deleteByRefreshToken(tokenHash);
            throw new InvalidTokenException("El usuario asociado al token se encuentra inactivo.");
        }

        // Rotación de tokens: se genera nuevo par de access y refresh tokens
        String newAccessToken = tokenServicePort.generateAccessToken(user.getId(), user.getEmail());
        OffsetDateTime newAccessTokenExp = tokenServicePort.getAccessTokenExpiration();
        String rawNewRefreshToken = tokenServicePort.generateRefreshToken();
        OffsetDateTime newRefreshTokenExp = tokenServicePort.getRefreshTokenExpiration();

        currentToken.setAccessToken(newAccessToken);
        currentToken.setAccessTokenExpiresAt(newAccessTokenExp);
        currentToken.setRefreshToken(tokenServicePort.hashToken(rawNewRefreshToken));
        currentToken.setRefreshTokenExpiresAt(newRefreshTokenExp);

        userTokenRepositoryPort.save(currentToken);

        return new TokenResult(newAccessToken, newAccessTokenExp, rawNewRefreshToken, newRefreshTokenExp);
    }
}
