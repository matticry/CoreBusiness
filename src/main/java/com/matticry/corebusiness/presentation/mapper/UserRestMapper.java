package com.matticry.corebusiness.presentation.mapper;

import com.matticry.corebusiness.application.dto.CreateUserCommand;
import com.matticry.corebusiness.application.dto.UpdateUserCommand;
import com.matticry.corebusiness.domain.model.User;
import com.matticry.corebusiness.presentation.dto.CreateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UpdateUserRequestDto;
import com.matticry.corebusiness.presentation.dto.UserResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper para convertir entre los DTOs de la capa de presentación,
 * los comandos de la capa de aplicación y los modelos de dominio.
 */
@Component
public class UserRestMapper {

    public CreateUserCommand toCommand(CreateUserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new CreateUserCommand(
                dto.name(),
                dto.lastname(),
                dto.dni(),
                dto.email(),
                dto.password()
        );
    }

    public UpdateUserCommand toCommand(UpdateUserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new UpdateUserCommand(
                dto.name(),
                dto.lastname(),
                dto.dni(),
                dto.email(),
                dto.password()
        );
    }

    public UserResponseDto toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .dni(user.getDni())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    public List<UserResponseDto> toResponseList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toResponse)
                .toList();
    }
}
