package com.matticry.corebusiness.application.usecase.auth;

import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import com.matticry.corebusiness.domain.port.out.auth.UserTokenRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock
    private UserTokenRepositoryPort userTokenRepositoryPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;

    @Test
    @DisplayName("Logout - Debe revocar el refresh token por su hash")
    void shouldRevokeRefreshTokenByHash() {
        when(tokenServicePort.hashToken("refresh-token")).thenReturn("hash-sha256");

        logoutUseCase.execute("refresh-token");

        verify(userTokenRepositoryPort).deleteByRefreshToken("hash-sha256");
    }

    @Test
    @DisplayName("Logout sin refresh token - No debe interactuar con el repositorio")
    void shouldIgnoreBlankRefreshToken() {
        logoutUseCase.execute("   ");

        verifyNoInteractions(userTokenRepositoryPort, tokenServicePort);
    }
}
