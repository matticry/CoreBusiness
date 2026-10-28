package com.matticry.corebusiness.infrastructure.config.security;

import com.jayway.jsonpath.JsonPath;
import com.matticry.corebusiness.domain.port.out.auth.TokenServicePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenServicePort tokenServicePort;

    @Test
    @DisplayName("Raíz de la aplicación - Debe redirigir a Swagger UI sin autenticación")
    void shouldRedirectRootToSwagger() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    @Test
    @DisplayName("OpenAPI - Debe ser público para poder autenticarse desde Swagger")
    void shouldExposeOpenApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/users']['get']['security'][0]['bearerAuth']").exists());
    }

    @Test
    @DisplayName("POST /api/v1/users sin token - Debe retornar 401 (endpoint protegido)")
    void shouldRequireAuthenticationToCreateUsers() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Registro libre + login - Debe emitir un token utilizable en endpoints protegidos")
    void shouldRegisterLoginAndUseIssuedToken() throws Exception {
        String email = "login.flow@empresa.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Login","lastname":"Flow","dni":"99999999",
                                 "email":"%s","password":"ClaveSegura123*"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"ClaveSegura123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = JsonPath.read(loginBody, "$.data.accessToken");

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].email", hasItem(email)));
    }

    @Test
    @DisplayName("Sin token - Debe retornar 401 con el envelope ApiResponse")
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    @DisplayName("Token con firma alterada - Debe retornar 401")
    void shouldReturnUnauthorizedWhenTokenSignatureIsAltered() throws Exception {
        String validToken = tokenServicePort.generateAccessToken(1L, "carlos.gomez@empresa.com");
        String alteredToken = validToken.substring(0, validToken.length() - 4) + "AAAA";

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + alteredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Token válido - Debe permitir el acceso al recurso protegido")
    void shouldAllowAccessWithValidToken() throws Exception {
        String validToken = tokenServicePort.generateAccessToken(1L, "carlos.gomez@empresa.com");

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Login público con credenciales inexistentes - Debe retornar 401 del dominio")
    void shouldReturnUnauthorizedEnvelopeForUnknownCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"nadie@empresa.com\",\"password\":\"ClaveSegura123*\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales de autenticación inválidas."));
    }

    @Test
    @DisplayName("Logout público con refresh token desconocido - Debe ser idempotente y retornar 204")
    void shouldAllowIdempotentLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"token-desconocido\"}"))
                .andExpect(status().isNoContent());
    }
}
