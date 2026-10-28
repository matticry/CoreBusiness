package com.matticry.corebusiness.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Envoltorio estándar para todas las respuestas de la API.
 *
 * @param <T> tipo del payload de datos
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        Map<String, String> errors,
        String correlationId,
        Instant timestamp
) {
}
