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
* **Manejo de Errores:** RFC 7807 (`ProblemDetail`) vía `@RestControllerAdvice`

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
    ├── dto/                                  # Request y Response HTTP con validaciones Bean Validation (@Valid)
    ├── mapper/                               # Mappers entre DTOs HTTP y modelos del caso de uso
    └── exception/
        └── GlobalExceptionHandler.java        # Captura global de errores con RFC 7807 (ProblemDetail)
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
* `domain.exception`: Excepciones no comprobadas (`RuntimeException`) específicas del negocio.

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
* **Controladores REST:**
  * Uso de `@RestController` y versionado de rutas (ej. `/api/v1/recursos`).
  * Devolver `ResponseEntity<T>` con códigos de estado HTTP semánticos (200 OK, 201 Created, 204 No Content).
  * Validar los payloads de entrada con `@Valid` en el `@RequestBody`.
* **Documentación con Swagger / OpenAPI:**
  * Anotar los controladores con `@Tag` y los endpoints con `@Operation` y `@ApiResponse`.
* **Manejo Global de Excepciones:**
  * Implementado en `GlobalExceptionHandler` extendiendo o devolviendo `ProblemDetail` (RFC 7807).

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de gestión de usuarios")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserRestMapper mapper;

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un usuario y publica un evento asíncrono")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        var command = mapper.toCommand(request);
        var createdUser = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createdUser));
    }
}
```

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
* **Pruebas de Controladores:** `@WebMvcTest` con `MockMvc` para verificar serialización, validación `@Valid` y códigos HTTP.
* **Pruebas de Repositorios:** `@DataJpaTest` con base de datos H2 en memoria (`h2`).
* **Pruebas de Integración:** `@SpringBootTest` para pruebas end-to-end simulando flujo completo.

---

## 6. Cuándo Aplica esta Guía

Este skill y sus lineamientos deben seguirse siempre que se realicen las siguientes tareas en **CoreBusiness**:
* Crear o modificar entidades de dominio, casos de uso o puertos.
* Crear adaptadores de persistencia JPA, migraciones Flyway o consultas en PostgreSQL.
* Implementar productores o consumidores de RabbitMQ o métodos `@Async`.
* Exponer nuevos endpoints REST con validaciones y documentación OpenAPI / Swagger.
* Escribir pruebas unitarias o de integración.
* Resolver dudas o refactorizaciones de arquitectura en el proyecto.
