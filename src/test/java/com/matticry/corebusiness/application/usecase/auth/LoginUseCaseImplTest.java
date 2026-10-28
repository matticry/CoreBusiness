package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.application.dto.auth.LoginCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.domain.exception.auth.InvalidCredentialsException;
import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import com.matticry.corebusiness.domain.port.out.user.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    private static final String EMAIL = "carlos.gomez@empresa.com";
    private static final String RAW_REFRESH_TOKEN = "refresh-token-opaco";
    private static final String HASHED_REFRESH_TOKEN = "hash-sha256";

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserTokenRepositoryPort userTokenRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private User user(boolean status) {
        return User.builder()
                .id(1L)
                .name("Carlos")
                .lastname("Gómez")
                .dni("71829304")
                .email(EMAIL)
                .password("hashed-password")
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Login exitoso - Debe guardar el refresh token hasheado y devolver el token opaco")
    void shouldLoginAndPersistHashedRefreshToken() {
        OffsetDateTime accessExp = OffsetDateTime.now().plusMinutes(15);
        OffsetDateTime refreshExp = OffsetDateTime.now().plusDays(7);

        when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user(true)));
        when(passwordEncoderPort.matches("ClaveSegura123*", "hashed-password")).thenReturn(true);
        when(tokenServicePort.generateAccessToken(1L, EMAIL)).thenReturn("access-token");
        when(tokenServicePort.getAccessTokenExpiration()).thenReturn(accessExp);
        when(tokenServicePort.generateRefreshToken()).thenReturn(RAW_REFRESH_TOKEN);
        when(tokenServicePort.getRefreshTokenExpiration()).thenReturn(refreshExp);
        when(tokenServicePort.hashToken(RAW_REFRESH_TOKEN)).thenReturn(HASHED_REFRESH_TOKEN);

        TokenResult result = loginUseCase.execute(new LoginCommand("  " + EMAIL.toUpperCase() + "  ", "ClaveSegura123*"));

        ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenRepositoryPort).deleteByUserId(1L);
        verify(userTokenRepositoryPort).save(captor.capture());

        UserToken saved = captor.getValue();
        assertThat(saved.getRefreshToken()).isEqualTo(HASHED_REFRESH_TOKEN);
        assertThat(saved.getAccessToken()).isEqualTo("access-token");
        assertThat(saved.getRefreshTokenExpiresAt()).isEqualTo(refreshExp);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo(RAW_REFRESH_TOKEN);
        assertThat(result.accessTokenExpiresAt()).isEqualTo(accessExp);
        assertThat(result.refreshTokenExpiresAt()).isEqualTo(refreshExp);
    }

    @Test
    @DisplayName("Login con contraseña incorrecta - Debe lanzar InvalidCredentialsException")
    void shouldRejectInvalidPassword() {
        when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user(true)));
        when(passwordEncoderPort.matches("incorrecta", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute(new LoginCommand(EMAIL, "incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userTokenRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Login de usuario inactivo - Debe lanzar InvalidCredentialsException")
    void shouldRejectInactiveUser() {
        when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(user(false)));

        assertThatThrownBy(() -> loginUseCase.execute(new LoginCommand(EMAIL, "ClaveSegura123*")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("inactivo");

        verify(userTokenRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Login de usuario inexistente - Debe lanzar InvalidCredentialsException")
    void shouldRejectUnknownUser() {
        when(userRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(new LoginCommand(EMAIL, "ClaveSegura123*")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
