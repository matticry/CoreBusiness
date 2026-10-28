package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.application.dto.auth.RefreshTokenCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.domain.exception.auth.InvalidTokenException;
import com.matticry.corebusiness.domain.model.auth.UserToken;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    private static final String RAW_REFRESH_TOKEN = "refresh-token-opaco";
    private static final String TOKEN_HASH = "hash-sha256";
    private static final String NEW_RAW_REFRESH_TOKEN = "nuevo-refresh-token";
    private static final String NEW_TOKEN_HASH = "nuevo-hash-sha256";
    private static final String EMAIL = "carlos.gomez@empresa.com";

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserTokenRepositoryPort userTokenRepositoryPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @InjectMocks
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    private UserToken userToken(OffsetDateTime refreshExpiresAt) {
        return UserToken.builder()
                .id(10L)
                .userId(1L)
                .accessToken("access-token-anterior")
                .refreshToken(TOKEN_HASH)
                .refreshTokenExpiresAt(refreshExpiresAt)
                .build();
    }

    private User user(boolean status) {
        return User.builder().id(1L).email(EMAIL).status(status).build();
    }

    @Test
    @DisplayName("Refresh válido - Debe rotar el refresh token almacenando su hash")
    void shouldRotateRefreshToken() {
        OffsetDateTime newAccessExp = OffsetDateTime.now().plusMinutes(15);
        OffsetDateTime newRefreshExp = OffsetDateTime.now().plusDays(7);

        when(tokenServicePort.hashToken(RAW_REFRESH_TOKEN)).thenReturn(TOKEN_HASH);
        when(userTokenRepositoryPort.findByRefreshToken(TOKEN_HASH))
                .thenReturn(Optional.of(userToken(OffsetDateTime.now().plusDays(1))));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user(true)));
        when(tokenServicePort.generateAccessToken(1L, EMAIL)).thenReturn("nuevo-access-token");
        when(tokenServicePort.getAccessTokenExpiration()).thenReturn(newAccessExp);
        when(tokenServicePort.generateRefreshToken()).thenReturn(NEW_RAW_REFRESH_TOKEN);
        when(tokenServicePort.getRefreshTokenExpiration()).thenReturn(newRefreshExp);
        when(tokenServicePort.hashToken(NEW_RAW_REFRESH_TOKEN)).thenReturn(NEW_TOKEN_HASH);

        TokenResult result = refreshTokenUseCase.execute(new RefreshTokenCommand("  " + RAW_REFRESH_TOKEN + "  "));

        ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenRepositoryPort).save(captor.capture());

        UserToken saved = captor.getValue();
        assertThat(saved.getAccessToken()).isEqualTo("nuevo-access-token");
        assertThat(saved.getAccessTokenExpiresAt()).isEqualTo(newAccessExp);
        assertThat(saved.getRefreshToken()).isEqualTo(NEW_TOKEN_HASH);
        assertThat(saved.getRefreshTokenExpiresAt()).isEqualTo(newRefreshExp);

        assertThat(result.refreshToken()).isEqualTo(NEW_RAW_REFRESH_TOKEN);
        assertThat(result.accessToken()).isEqualTo("nuevo-access-token");
    }

    @Test
    @DisplayName("Refresh desconocido - Debe lanzar InvalidTokenException sin persistir")
    void shouldRejectUnknownRefreshToken() {
        when(tokenServicePort.hashToken(RAW_REFRESH_TOKEN)).thenReturn(TOKEN_HASH);
        when(userTokenRepositoryPort.findByRefreshToken(TOKEN_HASH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN)))
                .isInstanceOf(InvalidTokenException.class);

        verify(userTokenRepositoryPort, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Refresh expirado - Debe eliminarlo y lanzar InvalidTokenException")
    void shouldRejectExpiredRefreshToken() {
        when(tokenServicePort.hashToken(RAW_REFRESH_TOKEN)).thenReturn(TOKEN_HASH);
        when(userTokenRepositoryPort.findByRefreshToken(TOKEN_HASH))
                .thenReturn(Optional.of(userToken(OffsetDateTime.now().minusMinutes(1))));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN)))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expirado");

        verify(userTokenRepositoryPort).deleteByRefreshToken(TOKEN_HASH);
    }

    @Test
    @DisplayName("Refresh de usuario inactivo - Debe revocar el token y lanzar InvalidTokenException")
    void shouldRejectRefreshForInactiveUser() {
        when(tokenServicePort.hashToken(RAW_REFRESH_TOKEN)).thenReturn(TOKEN_HASH);
        when(userTokenRepositoryPort.findByRefreshToken(TOKEN_HASH))
                .thenReturn(Optional.of(userToken(OffsetDateTime.now().plusDays(1))));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user(false)));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN)))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("inactivo");

        verify(userTokenRepositoryPort).deleteByRefreshToken(TOKEN_HASH);
    }
}
