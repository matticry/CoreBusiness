package com.matticry.corebusiness.application.usecase.user;

import com.matticry.corebusiness.application.dto.user.CreateUserCommand;
import com.matticry.corebusiness.domain.exception.user.UserAlreadyExistsException;
import com.matticry.corebusiness.domain.model.user.User;
import com.matticry.corebusiness.domain.port.out.user.PasswordEncoderPort;
import com.matticry.corebusiness.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private CreateUserUseCaseImpl createUserUseCase;

    @Test
    @DisplayName("Debe crear un usuario exitosamente cuando los datos son válidos")
    void shouldCreateUserSuccessfully() {
        CreateUserCommand command = new CreateUserCommand(
                "Carlos",
                "Gómez",
                "71829304",
                "carlos.gomez@empresa.com",
                "Password123*"
        );

        when(userRepositoryPort.existsByEmail("carlos.gomez@empresa.com")).thenReturn(false);
        when(userRepositoryPort.existsByDni("71829304")).thenReturn(false);
        when(passwordEncoderPort.encode("Password123*")).thenReturn("$2a$10$hashedPasswordSample");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = createUserUseCase.execute(command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Carlos", result.getName());
        assertEquals("Gómez", result.getLastname());
        assertEquals("71829304", result.getDni());
        assertEquals("carlos.gomez@empresa.com", result.getEmail());
        assertEquals("$2a$10$hashedPasswordSample", result.getPassword());
        assertTrue(result.getStatus());
        assertFalse(result.getEmailVerified());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException si el email ya está en uso")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "Carlos",
                "Gómez",
                "71829304",
                "carlos.gomez@empresa.com",
                "Password123*"
        );

        when(userRepositoryPort.existsByEmail("carlos.gomez@empresa.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> createUserUseCase.execute(command));
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException si el DNI ya está en uso")
    void shouldThrowExceptionWhenDniAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "Carlos",
                "Gómez",
                "71829304",
                "carlos.gomez@empresa.com",
                "Password123*"
        );

        when(userRepositoryPort.existsByEmail("carlos.gomez@empresa.com")).thenReturn(false);
        when(userRepositoryPort.existsByDni("71829304")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> createUserUseCase.execute(command));
        verify(userRepositoryPort, never()).save(any());
    }
}
