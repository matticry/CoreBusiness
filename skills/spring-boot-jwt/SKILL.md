---
name: spring-boot-jwt
description: Implementar autenticación JWT segura en APIs y microservicios Spring Boot (Java 17/21, Spring Security 6/7). Úsala SIEMPRE que el usuario mencione JWT, tokens Bearer, login/refresh token, Spring Security, SecurityFilterChain, autenticación o autorización de una API REST, roles/@PreAuthorize, o pida revisar una implementación de JWT existente, aunque no diga explícitamente "JWT seguro" o "buenas prácticas".
---

# Spring Boot JWT: implementación segura

Objetivo: que el agente implemente (o revise) JWT en Spring Boot sin errores de seguridad típicos, usando lo que Spring Security ya trae en vez de código criptográfico propio.

## Paso 1: decidir la arquitectura (preguntar solo lo que no se pueda inferir)

| Situación | Solución |
|---|---|
| Un solo servicio que emite y valida tokens | Resource Server nativo + `NimbusJwtEncoder` (HS256 o RSA) |
| Varios microservicios | Un servicio de auth firma con **RS256/ES256**; los demás solo validan con la clave pública (`jwk-set-uri` o `public-key-location`) |
| Ya existe un IdP (Keycloak, Auth0, Entra ID, Cognito) | NO emitir tokens propios: solo `issuer-uri` en el resource server |

**Regla por defecto: NO escribir un `OncePerRequestFilter` que parsee JWT a mano.** Usar `.oauth2ResourceServer(o -> o.jwt(...))`. Un filtro propio solo se justifica si el usuario lo exige; en ese caso aplicar igualmente todas las reglas del Paso 3.

## Paso 2: implementar

Leer el `pom.xml` para detectar la versión de Spring Boot: Boot 3.x = Security 6; Boot 4.x = Security 7. Los starters pueden cambiar de nombre en Boot 4, así que verificar contra la documentación oficial si la dependencia no resuelve.

Implementar, en el paquete de infraestructura/seguridad del proyecto:

- Dependencias Maven de Spring Security y OAuth2 Resource Server.
- `JwtProperties` con `@ConfigurationProperties`: issuer, audience, algoritmo y clave(s).
- `JwtConfig`: `NimbusJwtEncoder` y `NimbusJwtDecoder` (o `JwtDecoders.fromIssuerLocation`) con validación de issuer y audience.
- `TokenService`: emisión de access tokens con `jti`, `iat`, `sub`, `exp` y claims mínimos.
- `SecurityConfig`: `SecurityFilterChain` stateless, rutas públicas explícitas y handlers 401/403 serializados con `ObjectMapper` usando el envelope de errores del proyecto (`ApiResponse` en CoreBusiness).
- Conversión de roles a authorities desde el claim correspondiente.

Si hay login con refresh tokens (casi siempre), implementar rotación, detección de reuso, almacenamiento hasheado y la migración Flyway correspondiente.

## Paso 3: reglas obligatorias (checklist)

**Tokens**
- Access token corto: 5 a 15 min. Refresh token largo: 7 a 30 días, **opaco** (aleatorio, no JWT), guardado hasheado en BD, con rotación.
- Algoritmo fijado en el decoder (`macAlgorithm(HS256)` o RSA). Nunca aceptar el que diga el header, nunca `none`.
- Validar siempre `exp`, `iss` y `aud`. `JwtValidators.createDefaultWithIssuer` cubre `exp`/`nbf`/`iss`; agregar validador de `aud`.
- Incluir `jti` (id único) y `iat`. `sub` = id estable del usuario, no el email.
- Payload mínimo: sin contraseñas, datos personales ni secretos. El JWT va firmado, **no cifrado**.

**Claves**
- HS256: secreto de al menos 256 bits aleatorios, en variable de entorno o gestor de secretos. Nunca en el repo ni en `application.yml` con valor real.
- Microservicios: RS256/ES256, la clave privada solo en el servicio emisor.
- Contemplar rotación de claves (`kid` en el header).

**Spring Security**
- `SessionCreationPolicy.STATELESS`; CSRF deshabilitado solo si el token viaja en header `Authorization` (si va en cookie, mantener CSRF o usar `SameSite`).
- Listar rutas públicas explícitas (`/api/v1/auth/login`, `/register`, `/refresh`), no `/auth/**`.
- Incluir `/error` en `permitAll` (si no, los 404/500 se convierten en 401).
- Swagger/OpenAPI público solo en perfil `dev`.
- `BCryptPasswordEncoder` (strength 10 a 12) o `DelegatingPasswordEncoder`/Argon2.
- Respuestas 401/403 con `WWW-Authenticate: Bearer` y el envelope de errores del proyecto (`ApiResponse` en CoreBusiness, `ProblemDetail` en otros proyectos), serializadas con `ObjectMapper`, no JSON escrito a mano.
- Mismo mensaje de error para "usuario no existe" y "contraseña incorrecta".

**Operación**
- Logout = revocar refresh token (y opcionalmente lista de `jti` revocados con TTL = vida restante del access token).
- Rate limiting / bloqueo temporal en `/login`.
- CORS explícito si hay frontend en otro origen (nunca `*` con credenciales).
- No loguear tokens.

## Paso 4: pruebas

Generar con `@SpringBootTest` + `MockMvc` al menos estos casos: sin token → 401; token expirado → 401; firma alterada → 401; `aud` o `iss` incorrecto → 401; rol insuficiente → 403; token válido → 200; refresh reutilizado → toda la familia revocada.

## Revisión de código existente

Si el usuario pega una implementación, revisarla contra el Paso 3 y responder con: (1) qué está bien, (2) fallos ordenados por gravedad (críticos: algoritmo no fijado, sin validar `exp`/`aud`, secreto débil o hardcodeado; luego los demás), (3) el cambio concreto para cada uno.

## Qué NO hacer

- No usar jjwt/java-jwt con parseo manual si Nimbus + Spring ya cubren el caso.
- No guardar tokens en `localStorage` si hay frontend web: recomendar cookie `HttpOnly; Secure; SameSite`.
- No usar HS256 con claves compartidas entre muchos microservicios (cualquiera podría emitir tokens).
- No hacer access tokens de larga duración "para no complicarse con refresh".
