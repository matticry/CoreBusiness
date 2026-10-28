package com.matticry.corebusiness.presentation.controller;

import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.in.user.CreateUserUseCase;
import com.matticry.corebusiness.domain.port.in.user.DeleteUserUseCase;
import com.matticry.corebusiness.domain.port.in.user.GetAllUsersUseCase;
import com.matticry.corebusiness.domain.port.in.user.GetUserByIdUseCase;
import com.matticry.corebusiness.domain.port.in.user.UpdateUserUseCase;
import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import com.matticry.corebusiness.presentation.dto.CreateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UserResponseDto;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiResponseFactory.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private GetUserByIdUseCase getUserByIdUseCase;

    @MockitoBean
    private GetAllUsersUseCase getAllUsersUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private UserRestMapper mapper;

    @Test
    @DisplayName("POST /api/v1/users - Debe registrar un usuario y retornar 201 Created")
    void shouldCreateUser() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Carlos",
                "Gómez",
                "71829304",
                "carlos.gomez@empresa.com",
                "ClaveSegura123*"
        );

        User domainUser = User.builder()
                .id(1L)
                .name("Carlos")
                .lastname("Gómez")
                .dni("71829304")
                .email("carlos.gomez@empresa.com")
                .status(true)
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .name("Carlos")
                .lastname("Gómez")
                .dni("71829304")
                .email("carlos.gomez@empresa.com")
                .status(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .emailVerified(false)
                .build();

        when(mapper.toCommand(any(CreateUserRequestDto.class))).thenCallRealMethod();
        when(createUserUseCase.execute(any())).thenReturn(domainUser);
        when(mapper.toResponse(domainUser)).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Carlos"))
                .andExpect(jsonPath("$.data.email").value("carlos.gomez@empresa.com"));
    }

    @Test
    @DisplayName("POST /api/v1/users - Debe fallar con 400 Bad Request si los datos son inválidos")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        CreateUserRequestDto invalidRequest = new CreateUserRequestDto(
                "", // nombre vacío
                "",
                "123", // DNI muy corto
                "invalid-email",
                "123" // contraseña muy corta
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/users/1 - Debe retornar 200 OK con el usuario encontrado")
    void shouldGetUserById() throws Exception {
        User domainUser = User.builder().id(1L).name("Carlos").build();
        UserResponseDto response = UserResponseDto.builder().id(1L).name("Carlos").build();

        when(getUserByIdUseCase.execute(1L)).thenReturn(domainUser);
        when(mapper.toResponse(domainUser)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Carlos"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Debe retornar lista de usuarios")
    void shouldGetAllUsers() throws Exception {
        List<User> list = List.of(User.builder().id(1L).name("Carlos").build());
        List<UserResponseDto> responseList = List.of(UserResponseDto.builder().id(1L).name("Carlos").build());

        when(getAllUsersUseCase.execute()).thenReturn(list);
        when(mapper.toResponseList(list)).thenReturn(responseList);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/1 - Debe retornar 204 No Content")
    void shouldDeleteUser() throws Exception {
        doNothing().when(deleteUserUseCase).execute(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
