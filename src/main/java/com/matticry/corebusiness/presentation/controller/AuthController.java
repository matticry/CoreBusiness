package com.matticry.corebusiness.presentation.controller;

import com.matticry.corebusiness.domain.port.in.auth.LoginUseCase;
import com.matticry.corebusiness.domain.port.in.auth.LogoutUseCase;
import com.matticry.corebusiness.domain.port.in.auth.RefreshTokenUseCase;
import com.matticry.corebusiness.domain.port.in.user.CreateUserUseCase;
import com.matticry.corebusiness.presentation.dto.ApiResponse;
import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import com.matticry.corebusiness.presentation.dto.CreateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UserResponseDto;
import com.matticry.corebusiness.presentation.dto.auth.LoginRequestDto;
import com.matticry.corebusiness.presentation.dto.auth.RefreshTokenRequestDto;
import com.matticry.corebusiness.presentation.dto.auth.TokenResponseDto;
import com.matticry.corebusiness.presentation.mapper.AuthRestMapper;
import com.matticry.corebusiness.presentation.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación, renovación y revocación de tokens.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y gestión de tokens JWT")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final AuthRestMapper mapper;
    private final UserRestMapper userRestMapper;
    private final ApiResponseFactory apiResponseFactory;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un usuario nuevo para poder iniciar sesión. Ruta pública de autoregistro.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Correo electrónico o DNI ya registrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody CreateUserRequestDto request) {
        var user = createUserUseCase.execute(userRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponseFactory.created("Usuario registrado exitosamente.", userRestMapper.toResponse(user)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y emite un access token JWT y un refresh token opaco.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        var result = loginUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.ok(apiResponseFactory.ok("Inicio de sesión exitoso.", mapper.toResponse(result)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens", description = "Rota el refresh token y emite un nuevo par de access y refresh tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokens renovados exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh token ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido, revocado o expirado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        var result = refreshTokenUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.ok(apiResponseFactory.ok("Tokens renovados exitosamente.", mapper.toResponse(result)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token presentado. La operación es idempotente.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Sesión cerrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh token ausente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        logoutUseCase.execute(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
