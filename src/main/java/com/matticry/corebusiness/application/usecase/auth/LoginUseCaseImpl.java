package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.application.dto.auth.LoginCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.domain.exception.auth.InvalidCredentialsException;
import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.auth.LoginUseCase;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import com.matticry.corebusiness.domain.port.out.user.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Caso de uso para la autenticación de usuarios y emisión de pares de tokens JWT y Refresh Token.
 */
@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserTokenRepositoryPort userTokenRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenServicePort tokenServicePort;

    @Override
    @Transactional
    public TokenResult execute(LoginCommand command) {
        String normalizedEmail = command.email().trim().toLowerCase();

        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new InvalidCredentialsException("El usuario se encuentra inactivo.");
        }

        if (!passwordEncoderPort.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenServicePort.generateAccessToken(user.getId(), user.getEmail());
        OffsetDateTime accessTokenExp = tokenServicePort.getAccessTokenExpiration();
        String rawRefreshToken = tokenServicePort.generateRefreshToken();
        OffsetDateTime refreshTokenExp = tokenServicePort.getRefreshTokenExpiration();

        userTokenRepositoryPort.deleteByUserId(user.getId());

        UserToken userToken = UserToken.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .accessTokenExpiresAt(accessTokenExp)
                .refreshToken(tokenServicePort.hashToken(rawRefreshToken))
                .refreshTokenExpiresAt(refreshTokenExp)
                .createdAt(OffsetDateTime.now())
                .build();

        userTokenRepositoryPort.save(userToken);

        return new TokenResult(accessToken, accessTokenExp, rawRefreshToken, refreshTokenExp);
    }
}
