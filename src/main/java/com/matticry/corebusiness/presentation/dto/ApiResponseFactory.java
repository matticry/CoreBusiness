package com.matticry.corebusiness.presentation.dto;

import com.matticry.corebusiness.presentation.filter.RequestLoggingFilter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Construye respuestas {@link ApiResponse} incluyendo el identificador de correlación de la petición.
 */
@Component
public class ApiResponseFactory {

    public <T> ApiResponse<T> ok(T data) {
        return success(HttpStatus.OK, "Operación exitosa.", data);
    }

    public <T> ApiResponse<T> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public <T> ApiResponse<T> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

    public <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(true, status.value(), message, data, null, correlationId(), Instant.now());
    }

    public ApiResponse<Void> error(HttpStatus status, String message) {
        return error(status, message, null);
    }

    public ApiResponse<Void> error(HttpStatus status, String message, Map<String, String> errors) {
        return new ApiResponse<>(false, status.value(), message, null, errors, correlationId(), Instant.now());
    }

    private String correlationId() {
        return MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY);
    }
}
