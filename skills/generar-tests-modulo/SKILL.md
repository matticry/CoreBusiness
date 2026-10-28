---
name: generar-tests-modulo
description: Analiza módulos nuevos o modificados de CoreBusiness (clases de domain, application, infrastructure y presentation) e implementa los tests JUnit 5, Mockito, @WebMvcTest y @DataJpaTest que cubran sus funcionalidades. Úsala cuando se cree un módulo, caso de uso, endpoint, adaptador o funcionalidad nueva, cuando el usuario pida tests para código recién agregado, o cuando detectes clases de producción sin pruebas.
---

# Generar tests para un módulo nuevo

Objetivo: detectar qué funcionalidades nuevas se crearon en el repositorio, mapearlas al tipo de test correcto según Clean Architecture y dejar la suite en verde sin modificar el código de producción.

## Paso 1: detectar el alcance del módulo

1. Identifica los cambios nuevos o pendientes:

```powershell
git status --short
git diff --name-only
git diff --cached --name-only
```

Si el módulo ya está commiteado, compara contra la rama base o revisa los últimos commits con `git log --oneline` y `git show --stat`.

2. Agrupa los archivos nuevos por capa:

| Ruta | Qué contiene |
|---|---|
| `domain/model`, `domain/exception` | Reglas e invariantes de negocio |
| `domain/port/in`, `domain/port/out` | Contratos de casos de uso y salida |
| `application/dto`, `application/usecase` | Comandos, queries y orquestación |
| `infrastructure/adapter/persistence` | Entidades JPA, repositorios y mappers |
| `infrastructure/adapter/messaging` | Productores y consumidores RabbitMQ |
| `infrastructure/config` | Configuración de Spring, async, RabbitMQ, OpenAPI |
| `presentation/controller`, `presentation/dto`, `presentation/mapper` | API HTTP |
| `presentation/filter`, `presentation/exception` | Middleware y manejo global de errores |

3. Consulta el skill `core-business` (`skills/core-business/SKILL.md`) para confirmar las reglas de arquitectura y testing del proyecto.

4. Inventaria las funcionalidades reales, no solo las clases:
   - Endpoints HTTP y sus códigos de respuesta.
   - Métodos públicos de casos de uso y sus excepciones de dominio.
   - Validaciones Bean Validation en DTOs.
   - Consultas y restricciones de persistencia.
   - Eventos publicados o consumidos.
   - Funcionalidades transversales (filtros, advice, correlation ID).

## Paso 2: construir la matriz de cobertura

Ubica cada test en `src/test/java` con el mismo paquete que la clase de producción y nombre `<Clase>Test`.

| Elemento de producción | Tipo de test | Herramientas y foco |
|---|---|---|
| Modelo de dominio con lógica | Unitario puro, sin Spring | JUnit 5; invariantes, valores límite, excepciones |
| Caso de uso (`*UseCaseImpl`) | Unitario con Mockito | `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`; camino feliz, cada excepción de negocio y verificación de que no haya efectos secundarios |
| Mapper de dominio/rest/persistencia | Unitario puro | Todos los campos, entradas nulas y listas vacías |
| Controlador REST | `@WebMvcTest(controllers = XController.class)` + MockMvc | Estados HTTP, envelope `ApiResponse`, validación `@Valid`, `X-Correlation-Id` |
| Repositorio/adaptador JPA | `@DataJpaTest` con H2 | Consultas, mapeo entidad-dominio y restricciones; nunca PostgreSQL real |
| Adaptador RabbitMQ | Unitario con mocks | Verificar `RabbitTemplate`, routing key, payload y delegación del `@RabbitListener` al caso de uso |
| Configuración con lógica | Unitario | Solo si contiene lógica propia; no testear beans triviales |
| Filtro HTTP / advice global | `@WebMvcTest` o unitario | Headers, MDC, envelope y status por tipo de excepción |

No generes tests para `package-info.java`, records sin comportamiento, DTOs planos ni constantes.

## Paso 3: escribir los tests

Reglas obligatorias:

- JUnit 5 y Mockito. Usa `@DisplayName` en español describiendo el escenario.
- Usa aserciones JUnit (`assertEquals`, `assertThrows`, etc.) y `verify`/`never` de Mockito, siguiendo el estilo de `CreateUserUseCaseImplTest`.
- Un test por comportamiento relevante: camino feliz, validaciones, excepciones de dominio, bordes (nulo, vacío, no encontrado) y conflictos.
- No levantes PostgreSQL ni RabbitMQ reales. Usa `src/test/resources/application.properties` (H2, Flyway deshabilitado) y mocks.
- No modifiques código de producción para hacer pasar un test. Si encuentras un bug, repórtalo y propone el cambio por separado.
- No agregues dependencias nuevas sin consultar; usa las que ya están en `pom.xml`.

### Particularidades de este proyecto

- El envelope estándar es `com.matticry.corebusiness.presentation.dto.ApiResponse<T>` con `success`, `status`, `message`, `data`, `errors`, `correlationId` y `timestamp`.
- En tests de controlador exitosos verifica `$.success=true`, `$.status`, `$.data.<campo>` y `$.correlationId`.
- En errores verifica `$.success=false`, `$.status`, `$.message` y `$.errors.<campo>` cuando aplique; ya no se usa `ProblemDetail` (`$.title`).
- Los tests de controlador necesitan `@Import(ApiResponseFactory.class)` porque `@WebMvcTest` no escanea `@Component` planos; `GlobalExceptionHandler` y `RequestLoggingFilter` sí se incluyen por ser advice/filter.
- Usa `@MockitoBean` para los puertos de entrada y mappers en los tests de controlador.
- `DELETE` responde 204 sin body: valida solo el estado.
- Para filtrar el header de correlación usa `RequestLoggingFilter.CORRELATION_ID_HEADER`.

Ejemplo mínimo de test de controlador:

```java
@WebMvcTest(controllers = UserController.class)
@Import(ApiResponseFactory.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserByIdUseCase getUserByIdUseCase;

    @MockitoBean
    private UserRestMapper mapper;

    @Test
    @DisplayName("GET /api/v1/users/{id} - Debe retornar el usuario envuelto en ApiResponse")
    void shouldGetUserById() throws Exception {
        // arrange + act + assert
        // .andExpect(status().isOk())
        // .andExpect(jsonPath("$.success").value(true))
        // .andExpect(jsonPath("$.data.id").value(1L));
    }
}
```

## Paso 4: ejecutar y corregir

```powershell
.\mvnw.cmd -q -Dtest=NombreDeLaClaseTest test
.\mvnw.cmd -q test
```

1. Ejecuta primero los tests nuevos de forma aislada.
2. Corrige fallos reales de test; si el fallo revela un bug de producción, no lo maquilles: informa el hallazgo.
3. Ejecuta la suite completa y confirma que sigue en verde.

## Paso 5: entregar el resultado

Responde con:

- Archivos de test creados, agrupados por capa.
- Matriz funcionalidad → test → resultado.
- Comando ejecutado y resumen de la suite (`Tests run: X, Failures: 0, Errors: 0`).
- Bugs o huecos de cobertura detectados que requieran decisión del usuario.

## Checklist de finalización

- [ ] Cada endpoint nuevo tiene tests de éxito, validación y errores de dominio aplicables.
- [ ] Cada caso de uso nuevo tiene camino feliz, excepciones y verificación de dependencias.
- [ ] Los mappers nuevos cubren todos los campos, nulos y listas vacías.
- [ ] Los adaptadores de persistencia usan `@DataJpaTest` con H2.
- [ ] Los tests de controlador validan el envelope `ApiResponse` y `X-Correlation-Id`.
- [ ] No se modificó código de producción ni se agregaron dependencias.
- [ ] `.\mvnw.cmd -q test` termina sin fallos.
