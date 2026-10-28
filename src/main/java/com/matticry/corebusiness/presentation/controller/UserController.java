package com.matticry.corebusiness.presentation.controller;

import com.matticry.corebusiness.domain.port.in.user.CreateUserUseCase;
import com.matticry.corebusiness.domain.port.in.user.DeleteUserUseCase;
import com.matticry.corebusiness.domain.port.in.user.GetAllUsersUseCase;
import com.matticry.corebusiness.domain.port.in.user.GetUserByIdUseCase;
import com.matticry.corebusiness.domain.port.in.user.UpdateUserUseCase;
import com.matticry.corebusiness.presentation.dto.ApiResponse;
import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import com.matticry.corebusiness.presentation.dto.CreateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UpdateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UserResponseDto;
import com.matticry.corebusiness.presentation.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el CRUD y gestión del ciclo de vida de usuarios.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de gestión y administración de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserRestMapper mapper;
    private final ApiResponseFactory apiResponseFactory;

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un nuevo usuario con contraseña hasheada y validaciones de datos.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto: correo electrónico o DNI ya registrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        var command = mapper.toCommand(request);
        var createdUser = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponseFactory.created("Usuario creado exitosamente.", mapper.toResponse(createdUser)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Recupera la información de un usuario dado su identificador único.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @Parameter(description = "ID único del usuario", example = "1", required = true)
            @PathVariable Long id) {
        var user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(apiResponseFactory.ok("Usuario encontrado.", mapper.toResponse(user)));
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene la lista completa de usuarios registrados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        var users = getAllUsersUseCase.execute();
        return ResponseEntity.ok(apiResponseFactory.ok("Usuarios recuperados exitosamente.", mapper.toResponseList(users)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente (name, lastname, dni, email y password hasheado).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto: correo electrónico o DNI en uso por otro usuario",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @Parameter(description = "ID único del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        var command = mapper.toCommand(request);
        var updatedUser = updateUserUseCase.execute(id, command);
        return ResponseEntity.ok(apiResponseFactory.ok("Usuario actualizado exitosamente.", mapper.toResponse(updatedUser)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina permanentemente un usuario por su ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID único del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
