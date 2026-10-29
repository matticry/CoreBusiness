# ─────────────────────────────────────────────────────────────────
# Etapa 1: extraer capas del JAR de Spring Boot (layered jar)
# ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /app

# El JAR ya fue compilado y descargado por el pipeline CI/CD
COPY target/*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ─────────────────────────────────────────────────────────────────
# Etapa 2: imagen final mínima con capas ordenadas
# ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar capas en orden (dependencies cambian poco → cache óptimo)
COPY --from=builder /app/extracted/dependencies/           ./
COPY --from=builder /app/extracted/spring-boot-loader/     ./
COPY --from=builder /app/extracted/snapshot-dependencies/  ./
COPY --from=builder /app/extracted/application/            ./

# Puerto expuesto por Spring Boot
EXPOSE 8080

# Health check usando el actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
