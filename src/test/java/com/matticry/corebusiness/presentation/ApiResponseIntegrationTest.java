package com.matticry.corebusiness.presentation;

import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import com.matticry.corebusiness.presentation.filter.RequestLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@WithMockUser
@Import({ApiResponseFactory.class, RequestLoggingFilter.class})
class ApiResponseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldWrapSuccessfulResponseAndPropagateCorrelationId() throws Exception {
        mockMvc.perform(get("/test/ok").header(RequestLoggingFilter.CORRELATION_ID_HEADER, "corr-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestLoggingFilter.CORRELATION_ID_HEADER, "corr-123"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Operación exitosa."))
                .andExpect(jsonPath("$.data").value("hola"))
                .andExpect(jsonPath("$.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestLoggingFilter.CORRELATION_ID_HEADER))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void shouldReturnValidationErrorsInResponseEnvelope() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
