package com.matticry.corebusiness.presentation.controller;

import com.matticry.corebusiness.application.dto.auth.LoginCommand;
import com.matticry.corebusiness.application.dto.auth.RefreshTokenCommand;
import com.matticry.corebusiness.application.dto.auth.TokenResult;
import com.matticry.corebusiness.domain.exception.auth.InvalidCredentialsException;
import com.matticry.corebusiness.domain.exception.auth.InvalidTokenException;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.auth.LoginUseCase;
import com.matticry.corebusiness.domain.port.in.auth.LogoutUseCase;
import com.matticry.corebusiness.domain.port.in.auth.RefreshTokenUseCase;
import com.matticry.corebusiness.domain.port.in.user.CreateUserUseCase;
import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import com.matticry.corebusiness.presentation.dto.CreateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UserResponseDto;
import com.matticry.corebusiness.presentation.dto.auth.LoginRequestDto;
import com.matticry.corebusiness.presentation.dto.auth.RefreshTokenRequestDto;
import com.matticry.corebusiness.presentation.mapper.AuthRestMapper;
import com.matticry.corebusiness.presentation.mapper.UserRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiResponseFactory.class, AuthRestMapper.class})
class AuthControllerTest {

    private static final String RAW_REFRESH_TOKEN = "cmVmcmVzaC10b2tlbi1vcGFjbw";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private UserRestMapper userRestMapper;

    private TokenResult tokenResult() {
        OffsetDateTime now = OffsetDateTime.now();
        return new TokenResult(
                "access-token-jwt",
                now.plusMinutes(15),
                RAW_REFRESH_TOKEN,
                now.plusDays(7)
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe registrar el usuario y retornar 201")
    void shouldRegisterUser() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Carlos", "Gómez", "71829304", "carlos.gomez@empresa.com", "ClaveSegura123*");
        User domainUser = User.builder().id(1L).email("carlos.gomez@empresa.com").build();
        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .email("carlos.gomez@empresa.com")
                .build();

        when(userRestMapper.toCommand(any(CreateUserRequestDto.class))).thenReturn(null);
        when(createUserUseCase.execute(any())).thenReturn(domainUser);
        when(userRestMapper.toResponse(domainUser)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.email").value("carlos.gomez@empresa.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe retornar 200 con el par de tokens")
    void shouldLoginSuccessfully() throws Exception {
        when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(tokenResult());

        LoginRequestDto request = new LoginRequestDto("carlos.gomez@empresa.com", "ClaveSegura123*");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.data.refreshToken").value(RAW_REFRESH_TOKEN))
                .andExpect(jsonPath("$.data.accessTokenExpiresAt").exists())
                .andExpect(jsonPath("$.data.refreshTokenExpiresAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe retornar 401 si las credenciales son inválidas")
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(loginUseCase.execute(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialsException());

        LoginRequestDto request = new LoginRequestDto("carlos.gomez@empresa.com", "ClaveIncorrecta123*");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales de autenticación inválidas."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe retornar 400 si los datos son inválidos")
    void shouldReturnBadRequestWhenLoginPayloadIsInvalid() throws Exception {
        LoginRequestDto request = new LoginRequestDto("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debe retornar 200 con los tokens rotados")
    void shouldRefreshTokensSuccessfully() throws Exception {
        when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class))).thenReturn(tokenResult());

        RefreshTokenRequestDto request = new RefreshTokenRequestDto(RAW_REFRESH_TOKEN);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.data.refreshToken").value(RAW_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debe retornar 401 si el refresh token es inválido")
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
        when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class)))
                .thenThrow(new InvalidTokenException("Refresh token inválido o revocado."));

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("token-invalido");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Debe retornar 204 sin cuerpo")
    void shouldLogoutSuccessfully() throws Exception {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto(RAW_REFRESH_TOKEN);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute(RAW_REFRESH_TOKEN);
    }
}
