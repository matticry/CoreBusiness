---
name: core-business
description: Guía de arquitectura y estándares de CoreBusiness (Clean Architecture/Hexagonal, Java 21, Spring Boot 4, JPA/Flyway, RabbitMQ, @Async, endpoints REST con envelope ApiResponse, OpenAPI y testing). Úsala al crear o modificar módulos, entidades, casos de uso, puertos, adaptadores, controladores, endpoints, migraciones, middleware o al revisar refactorizaciones y decisiones de estructura de paquetes en CoreBusiness.
---

# CoreBusiness - Guía de Arquitectura y Estándares de Desarrollo

Este documento define el contexto, arquitectura, lineamientos técnicos y estándares de desarrollo para el microservicio **CoreBusiness** (`com.matticry.corebusiness`).

---

## 1. Stack Tecnológico

* **Lenguaje:** Java 21+
* **Framework Principal:** Spring Boot 4.x / 3.x
* **Patrón de Arquitectura:** Clean Architecture (Arquitectura Limpia / Hexagonal - Puertos y Adaptadores)
* **Base de Datos:** PostgreSQL
* **Persistencia:** Spring Data JPA / Hibernate
* **Migraciones de Base de Datos:** Flyway (`src/main/resources/db/migration`)
* **Mensajería Asíncrona:** RabbitMQ (`spring-boot-starter-amqp`, `spring-rabbit-stream`) con serialización Jackson JSON
* **Programación Asíncrona:** Spring `@Async` con `ThreadPoolTaskExecutor` dedicado
* **Documentación de API:** SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
* **Productividad:** Lombok (con constructor injection y procesador de anotaciones)
* **Manejo de Errores:** Envelope `ApiResponse<T>` vía `@RestControllerAdvice` (`GlobalExceptionHandler`) y `ApiResponseFactory`
* **Trazabilidad:** Filtro `RequestLoggingFilter` con `X-Correlation-Id` y MDC

---

## 2. Estructura de Paquetes (Clean Architecture)

El proyecto organiza el código en capas concéntricas cumpliendo estrictamente la **Regla de Dependencia** (las capas externas conocen a las internas, nunca al revés):

```text
com.matticry.corebusiness
│
├── CoreBusinessApplication.java              # Clase de inicio Spring Boot
│
├── domain/                                   # NÚCLEO: Reglas de negocio puras (POJOs, sin frameworks ni JPA)
│   ├── model/                                # Entidades y Value Objects de negocio
│   ├── port/
│   │   ├── in/                               # Puertos de Entrada: Interfaces de Casos de Uso
│   │   └── out/                              # Puertos de Salida: Interfaces de Repositorios y Eventos/Mensajería
│   └── exception/                            # Excepciones de negocio propias del dominio
│
├── application/                              # CASOS DE USO: Orquestación del flujo de la aplicación
│   ├── usecase/                              # Implementación de los puertos de entrada (inbound ports)
│   └── dto/                                  # DTOs internos (comandos, queries y resultados)
│
├── infrastructure/                           # INFRAESTRUCTURA: Adaptadores de salida y frameworks
│   ├── adapter/
│   │   ├── persistence/                      # Adaptador PostgreSQL / JPA
│   │   │   ├── entity/                       # Entidades JPA (@Entity, @Table, @Id)
│   │   │   ├── repository/                   # Interfaces Spring Data JPA
│   │   │   └── mapper/                       # Mappers entre Entidades JPA y Modelos de Dominio
│   │   └── messaging/
│   │       └── rabbitmq/                     # Consumidores (@RabbitListener) y Productores RabbitMQ
│   └── config/                               # Clases de configuración de Spring
│       ├── AsyncConfig.java                  # Habilita @EnableAsync y el pool de hilos
│       ├── OpenApiConfig.java                # Metadatos de OpenAPI 3 / Swagger
│       └── RabbitMQConfig.java               # Configuración de RabbitTemplate y Jackson converter
│
└── presentation/                             # PRESENTACIÓN / REST API: Adaptador de entrada HTTP
    ├── controller/                           # Controladores REST (@RestController) documentados con OpenAPI
    ├── dto/                                  # Request/Response HTTP, ApiResponse<T> y ApiResponseFactory
    ├── mapper/                               # Mappers entre DTOs HTTP y modelos del caso de uso
    ├── filter/                               # RequestLoggingFilter (correlation ID + logging)
    └── exception/
        └── GlobalExceptionHandler.java        # Captura global de errores con ApiResponse
```

---

## 3. Lineamientos y Reglas de Código

### Inyección de Dependencias
* **Regla estricta:** Usar **inyección por constructor**, nunca inyección de campo con `@Autowired`.
* Marcar los atributos inyectados como `private final`.
* Usar `@RequiredArgsConstructor` de Lombok para generar los constructores limpios.

```java
// Correcto
@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final UserEventPublisherPort eventPublisherPort;
}
```

---

### Capa de Dominio (`domain`)
* **Agnóstica a Frameworks:** Ninguna clase en `domain` debe importar librerías de Spring, Hibernate, JPA o Jackson.
* `domain.model`: Contiene entidades y agregados con lógica de negocio rica, validaciones invariantes y reglas de dominio.
* `domain.port.in`: Interfaces que definen los casos de uso disponibles.
* `domain.port.out`: Interfaces (SPIs) para persistencia, mensajería o clientes externos.
* `domain.exception`: Excepciones no comprobadas (`RuntimeException`) específicas del negocio (p. ej. `UserNotFoundException`, `UserAlreadyExistsException`).

---

### Capa de Aplicación (`application`)
* Implementa las interfaces de `domain.port.in`.
* Orquesta las entidades de dominio y coordina llamadas a los `domain.port.out`.
* Control transaccional con `@Transactional` en las implementaciones de casos de uso si modifican estado.
* Debe trabajar con DTOs de comando/query si la operación involucra parámetros complejos.

---

### Capa de Infraestructura (`infrastructure`)
* **Persistencia:**
  * Las entidades JPA viven únicamente en `infrastructure.adapter.persistence.entity` con sus respectivas anotaciones `@Entity`, `@Table`, `@Id`.
  * Nunca exponer las entidades JPA a las capas de aplicación o presentación.
  * Usar mappers para transformar `JpaEntity` <-> `DomainModel`.
  * Los repositorios de Spring Data (`JpaRepository`) viven en `infrastructure.adapter.persistence.repository`.
* **RabbitMQ & Mensajería:**
  * Consumidores anotados con `@RabbitListener` reciben payloads JSON y delegan en casos de uso.
  * Productores implementan puertos de salida del dominio (`domain.port.out`) y utilizan `RabbitTemplate` para emitir eventos.
* **Programación Asíncrona:**
  * Usar la anotación `@Async("taskExecutor")` para tareas en segundo plano que no bloqueen la respuesta HTTP.

---

### Capa de Presentación (`presentation`)

#### Reglas de controladores
* Uso de `@RestController` y versionado de rutas (`/api/v1/...`).
* Inyección por constructor con `@RequiredArgsConstructor`; el controlador depende de puertos de entrada (`*UseCase`), del mapper REST y de `ApiResponseFactory`.
* **Toda respuesta con body se envuelve en `ApiResponse<T>`**. Nunca devolver el DTO, la lista o la entidad desnuda.
* Validar la entrada con `@Valid` en `@RequestBody` y con las anotaciones correspondientes en parámetros de path/query.
* No construir `ApiResponse` con `new`: usar siempre `ApiResponseFactory`.
* No capturar excepciones en el controlador para formatearlas; delegar en `GlobalExceptionHandler`.
* `DELETE` correcto: `ResponseEntity.noContent().build()` → 204 sin body (no aplica envelope).

#### Contrato del envelope `ApiResponse<T>`
Definido en `presentation/dto/ApiResponse.java`:

| Campo | Tipo | Descripción |
|---|---|---|
| `success` | `boolean` | `true` en respuestas 2xx, `false` en errores |
| `status` | `int` | Código HTTP replicado en el body |
| `message` | `String` | Mensaje legible para el consumidor |
| `data` | `T` | Payload de la operación; `null` en errores |
| `errors` | `Map<String,String>` | Campo → mensaje; `null` en éxitos |
| `correlationId` | `String` | Igual al header `X-Correlation-Id` |
| `timestamp` | `Instant` | Momento de la respuesta (ISO-8601 UTC) |

* `@JsonInclude(NON_NULL)` omite `data`, `errors` o `correlationId` cuando son nulos.
* El `correlationId` lo obtiene la factory del MDC (`RequestLoggingFilter`); nunca pasarlo a mano.
* Un solo envelope por endpoint; no anidar `ApiResponse` dentro de otro `ApiResponse`.

Éxito con objeto:

```json
{
  "success": true,
  "status": 200,
  "message": "Usuario encontrado.",
  "data": { "id": 1, "name": "Carlos" },
  "correlationId": "6f1c9e0a-...",
  "timestamp": "2026-10-28T16:30:00Z"
}
```

Éxito con lista: mismo formato, pero `data` es un array.

Error de validación:

```json
{
  "success": false,
  "status": 400,
  "message": "Error en la validación de los datos enviados.",
  "errors": { "email": "debe ser una dirección de correo válida" },
  "correlationId": "6f1c9e0a-...",
  "timestamp": "2026-10-28T16:30:00Z"
}
```

#### `ApiResponseFactory`
Ubicada en `presentation/dto/ApiResponseFactory.java`:

| Método | Uso |
|---|---|
| `ok(T data)` | 200 con mensaje por defecto |
| `ok(String message, T data)` | 200 con mensaje propio |
| `created(String message, T data)` | 201 Created |
| `success(HttpStatus status, String message, T data)` | otros códigos 2xx |
| `error(HttpStatus status, String message)` | error sin detalle por campo |
| `error(HttpStatus status, String message, Map<String,String> errors)` | error con detalle por campo |

#### Códigos HTTP y cuándo usarlos
| Operación | Código | Body |
|---|---|---|
| GET colección o recurso | 200 | `ApiResponse<T>` |
| POST creación | 201 | `ApiResponse<T>` con `created(...)`; opcional header `Location` |
| PUT / PATCH | 200 | `ApiResponse<T>` |
| DELETE | 204 | Sin body |
| Validación, JSON mal formado o tipo inválido | 400 | `ApiResponse<Void>` con `errors` |
| Recurso inexistente | 404 | `ApiResponse<Void>` |
| Método HTTP no soportado | 405 | `ApiResponse<Void>` |
| Conflicto de negocio (duplicado) | 409 | `ApiResponse<Void>` |
| Error inesperado | 500 | `ApiResponse<Void>` con mensaje genérico |

#### Ejemplos por operación

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de gestión de usuarios")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserRestMapper mapper;
    private final ApiResponseFactory apiResponseFactory;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        var users = getAllUsersUseCase.execute();
        return ResponseEntity.ok(
                apiResponseFactory.ok("Usuarios recuperados exitosamente.", mapper.toResponseList(users)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        var user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(apiResponseFactory.ok("Usuario encontrado.", mapper.toResponse(user)));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un usuario y publica un evento asíncrono")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email o DNI ya registrado")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        var createdUser = createUserUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponseFactory.created("Usuario creado exitosamente.", mapper.toResponse(createdUser)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        var updatedUser = updateUserUseCase.execute(id, mapper.toCommand(request));
        return ResponseEntity.ok(
                apiResponseFactory.ok("Usuario actualizado exitosamente.", mapper.toResponse(updatedUser)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### Manejo global de excepciones
`GlobalExceptionHandler` (`@RestControllerAdvice`) devuelve siempre `ResponseEntity<ApiResponse<Void>>`:

| Excepción | Código |
|---|---|
| `MethodArgumentNotValidException` | 400 + `errors` por campo |
| `HandlerMethodValidationException` | 400 + errores de parámetros |
| `ConstraintViolationException` | 400 |
| `HttpMessageNotReadableException` | 400 cuerpo inválido o mal formado |
| `MissingServletRequestParameterException` | 400 parámetro faltante |
| `MethodArgumentTypeMismatchException` | 400 tipo inválido |
| `UserNotFoundException` (dominio) | 404 |
| `HttpRequestMethodNotSupportedException` | 405 |
| `UserAlreadyExistsException` (dominio) | 409 |
| `Exception` | 500 genérico + `log.error` |

Reglas:
* Cada excepción de dominio nueva debe tener su `@ExceptionHandler` con el status semántico correcto.
* Los 5xx usan un mensaje fijo; nunca exponer `ex.getMessage()`, stack traces, SQL ni nombres de tablas.
* Los 4xx pueden usar el mensaje de la excepción de dominio (es información de negocio).
* Usar `LinkedHashMap` para conservar el orden de los errores de validación.
* No duplicar el manejo de errores en los controladores.

#### Middleware y correlation ID
* `RequestLoggingFilter` (`OncePerRequestFilter`, `HIGHEST_PRECEDENCE`) lee o genera `X-Correlation-Id`, lo guarda en MDC y lo devuelve en el header de respuesta.
* `ApiResponseFactory` lee ese MDC; por eso el `correlationId` del body coincide con el header.
* No loguear cuerpos, tokens ni datos sensibles; solo método, ruta, status y duración.

#### Documentación OpenAPI
* Anotar el controlador con `@Tag` y cada endpoint con `@Operation` y `@ApiResponse`.
* El DTO `ApiResponse` colisiona con la anotación de Swagger: calificar la anotación como `@io.swagger.v3.oas.annotations.responses.ApiResponse`.
* Documentar todos los status posibles y usar `@Schema(implementation = ApiResponse.class)` para el envelope.
* Mantener los `@Schema` de los DTOs de request/response con descripciones y ejemplos.

#### Checklist de un endpoint nuevo
- [ ] Envelope `ApiResponse<T>` en todas las respuestas con body.
- [ ] `ApiResponseFactory` usada en lugar de `new ApiResponse<>(...)`.
- [ ] Código HTTP semántico (200, 201, 204, 400, 404, 405, 409, 500).
- [ ] `@Valid` y validaciones en request/path/query.
- [ ] Excepciones de dominio mapeadas en `GlobalExceptionHandler`.
- [ ] OpenAPI actualizado (`@Operation`, `@ApiResponse`).
- [ ] Tests de controlador con `@WebMvcTest`, envelope y `X-Correlation-Id`.

---

## 4. Base de Datos y Migraciones con Flyway

* **Ubicación:** `src/main/resources/db/migration`
* **Nomenclatura:** `V{Version}__{Descripcion}.sql` (ejemplo: `V1__create_users_table.sql`).
* **Configuración:**
  * `spring.jpa.hibernate.ddl-auto=validate` (Hibernate nunca debe crear o modificar tablas automáticamente en entornos de microservicio; Flyway tiene el control total de la DDL).
  * `spring.flyway.baseline-on-migrate=true`

---

## 5. Estrategia de Pruebas (Testing)

* **Pruebas de Casos de Uso / Dominio:** Pruebas unitarias puras con JUnit 5 y Mockito (`@ExtendWith(MockitoExtension.class)`), sin cargar el contexto de Spring Boot para ejecuciones ultrarrápidas.
* **Pruebas de Controladores:** `@WebMvcTest` con `MockMvc` para verificar serialización, validación `@Valid`, códigos HTTP, envelope `ApiResponse` y `X-Correlation-Id`. En éxitos validar `$.success`, `$.status`, `$.data.<campo>` y `$.correlationId`; en errores validar `$.success=false`, `$.status`, `$.message` y `$.errors.<campo>`. Importar `ApiResponseFactory` con `@Import` porque `@WebMvcTest` no escanea `@Component` planos.
* **Pruebas de Repositorios:** `@DataJpaTest` con base de datos H2 en memoria (`h2`).
* **Pruebas de Integración:** `@SpringBootTest` para pruebas end-to-end simulando flujo completo.
* **Ejecución:** `.\mvnw.cmd -q -Dtest=NombreTest test` para un test puntual y `.\mvnw.cmd -q test` para la suite completa.
* Si se crea un módulo nuevo, usar el skill `generar-tests-modulo` para generar la cobertura.

---

## 6. Cuándo Aplica esta Guía

Este skill y sus lineamientos deben seguirse siempre que se realicen las siguientes tareas en **CoreBusiness**:
* Crear o modificar entidades de dominio, casos de uso o puertos.
* Crear adaptadores de persistencia JPA, migraciones Flyway o consultas en PostgreSQL.
* Implementar productores o consumidores de RabbitMQ o métodos `@Async`.
* Exponer nuevos endpoints REST con validaciones y documentación OpenAPI / Swagger.
* Implementar filtros HTTP, middleware o manejo global de errores.
* Escribir pruebas unitarias o de integración.
* Resolver dudas o refactorizaciones de arquitectura en el proyecto.
