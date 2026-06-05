---
name: API Design & Integration
slug: nova-api-design
description: Diseño de APIs REST con OpenAPI 3.0, generación de código cliente, configuración de Feign Clients, Spring Cloud Gateway, y patrones de resilencia para servicios NOVA.
---

# API Design & Integration — NOVA

## OpenAPI 3.0 Spec Template

```yaml
openapi: "3.0.3"
info:
  title: API ${servicio.nombre}
  version: "1.0.0"
  description: ${servicio.descripcion}
  contact:
    name: Equipo NOVA
    email: equipo@bbva.com

servers:
  - url: http://localhost:8080
    description: Local (NOVA Click)
  - url: https://int.nova.bbva.com/${servicio.nombre}
    description: Integrado
  - url: https://nova.bbva.com/${servicio.nombre}
    description: Producción

paths:
  /api/v1/${recurso}:
    get:
      operationId: listar${Recurso}
      summary: Listar ${recurso}s con paginación
      tags: [${Recurso}s]
      parameters:
        - $ref: '#/components/parameters/PageParam'
        - $ref: '#/components/parameters/SizeParam'
      responses:
        '200':
          description: Lista paginada
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Page${Recurso}'
    post:
      operationId: crear${Recurso}
      summary: Crear nuevo ${recurso}
      tags: [${Recurso}s]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Create${Recurso}Request'
      responses:
        '201':
          description: Creado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/${Recurso}DTO'
        '400':
          $ref: '#/components/responses/BadRequest'

  /api/v1/${recurso}/{id}:
    get:
      operationId: obtener${Recurso}
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/${Recurso}DTO'
        '404':
          $ref: '#/components/responses/NotFound'

components:
  parameters:
    PageParam:
      name: page
      in: query
      schema: { type: integer, default: 0 }
    SizeParam:
      name: size
      in: query
      schema: { type: integer, default: 20, maximum: 100 }

  responses:
    BadRequest:
      description: Datos inválidos
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFound:
      description: Recurso no encontrado
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

  schemas:
    ErrorResponse:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
        timestamp:
          type: string
          format: date-time
```

## Generación de código con NOVA CLI (`nova generate-api-code`)

El CLI genera código desde la configuración de APIs del `nova.yml`:

```bash
# Ejecutar en directorio con nova.yml
nova generate-api-code
```

### Flavours de generación por tipo de servicio

| Tipo | Lenguaje | served (server) | consumed (client) | client_feign |
|------|----------|------------------|--------------------|--------------|
| API | Java - Spring boot | `spring.nova` | `jaxrs.nova` | `feign.nova` |
| API | Python - Flask | `python3` | `python3` | — |
| Batch | Java | — | `jaxrs.nova` | — |
| Daemon | Java | — | `jaxrs.nova` | `feign.nova` |
| CDN | Angular | TypeScript | TypeScript | — |

### Versión del generador JAR

Para CLI 7.8.0: Java 8 → `com.bbva.enoa.generator-1.9.11.jar`, Java 11 → `com.bbva.enoa.generator-2.9.2.jar`

### Post-generación

- **Java**: el CLI ejecuta `mvn_install.cmd/sh` para instalar como artefacto Maven local
- **Angular**: genera en `api-generated/` → ejecutar `node prepare-apis-generated.js`
  - Compila cada librería (`npm install` + `ng build`)
  - Instala `lib-generated/dist` como dependencia local del proyecto
  - También procesa `asyncapi-generated/backToFront/serverpush/client`
- **Python**: genera en `api-generated/` → añadir a `requirements.in`

## Generación de código cliente (Maven Plugin)

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>6.6.0</version>
    <executions>
        <execution>
            <id>generate-client</id>
            <goals><goal>generate</goal></goals>
            <configuration>
                <inputSpec>${project.basedir}/swagger/api-externa.yaml</inputSpec>
                <generatorName>java</generatorName>
                <library>spring-cloud</library>
                <apiPackage>com.bbva.nova.client.api</apiPackage>
                <modelPackage>com.bbva.nova.client.model</modelPackage>
                <configOptions>
                    <dateLibrary>java8</dateLibrary>
                    <useSpringBoot3>false</useSpringBoot3>
                    <interfaceOnly>true</interfaceOnly>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Feign Client Pattern

```java
@FeignClient(
    name = "servicio-externo",
    url = "${apis.externo.url:}",
    fallbackFactory = ExternoFallbackFactory.class,
    configuration = FeignClientConfig.class
)
public interface ExternoClient {

    @GetMapping("/api/v1/recursos/{id}")
    RecursoDTO obtener(@PathVariable("id") String id);

    @PostMapping("/api/v1/recursos")
    RecursoDTO crear(@RequestBody CreateRequest request);

    @GetMapping("/api/v1/recursos")
    PageResponse<RecursoDTO> listar(
        @RequestParam("page") int page,
        @RequestParam("size") int size);
}
```

## Feign Configuration

```java
@Configuration
public class FeignClientConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(5, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new NovaErrorDecoder();
    }

    @Bean
    public RequestInterceptor authInterceptor() {
        return template -> {
            template.header("X-NOVA-Service", "mi-servicio");
            template.header("X-Request-Id", UUID.randomUUID().toString());
        };
    }
}
```

## Spring Cloud Gateway Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ${servicio}
          uri: lb://${SERVICIO_UPPERCASE}
          predicates:
            - Path=/api/v1/${recurso}/**
          filters:
            - StripPrefix=0
            - name: CircuitBreaker
              args:
                name: ${servicio}-cb
                fallbackUri: forward:/fallback/${servicio}
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
                methods: GET
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
```

## Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        record-exceptions:
          - java.io.IOException
          - java.net.SocketTimeoutException
          - feign.FeignException
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        retry-exceptions:
          - java.io.IOException
  timelimiter:
    configs:
      default:
        timeout-duration: 5s
```

## Convenciones de naming

| Recurso | URL | Operación |
|---------|-----|-----------|
| Lista | GET /api/v1/recursos | listarRecursos |
| Obtener | GET /api/v1/recursos/{id} | obtenerRecurso |
| Crear | POST /api/v1/recursos | crearRecurso |
| Actualizar | PUT /api/v1/recursos/{id} | actualizarRecurso |
| Eliminar | DELETE /api/v1/recursos/{id} | eliminarRecurso |
| Acción | POST /api/v1/recursos/{id}/accion | ejecutarAccion |
