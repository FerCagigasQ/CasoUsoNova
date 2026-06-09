# GDPD Backend — Guía de Configuración de Actuator y Logging

Archivos de configuración para Spring Boot Actuator, métricas y logging estructurado.

## Archivos

- **`application.yml`** — Configuración centralizada de Actuator, datasource, Sleuth
- **`logback-spring.xml`** — Logging con perfiles (dev, int, pre, pro)
- **`CONFIGURATION.md`** — Esta guía

## Pasos de Implementación

### 1. Copiar Configuración Base

Copia a cada servicio en `src/main/resources/`:

```bash
cp application.yml gdpd-pedidos-api/src/main/resources/
cp logback-spring.xml gdpd-pedidos-api/src/main/resources/
```

### 2. Personalizar por Servicio

Edita `application.yml`:

```yaml
spring:
  application:
    name: gdpd-pedidos-api    # Cambiar según servicio

server:
  port: 8080                  # Puerto por servicio
```

**Puertos por servicio**:
- `gdpd-pedidos-api`: 8080
- `gdpd-event-processor`: 9080
- `gdpd-report-batch`: 9081
- `gdpd-report-scheduler`: 9082

### 3. Implementar Health Indicators

**DatabaseHealthIndicator** (API):

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Autowired private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? Health.up().build() : Health.down().build();
        } catch (SQLException e) {
            return Health.down().withException(e).build();
        }
    }
}
```

**BrokerHealthIndicator** (Demonio):

```java
@Component
public class BrokerHealthIndicator implements HealthIndicator {
    @Autowired private JmsTemplate jmsTemplate;
    
    @Override
    public Health health() {
        try {
            jmsTemplate.convertAndSend("health-check", "ping");
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

### 4. Implementar Métricas Custom

```java
@Component
public class PedidosMetricas {
    private final Counter contador;
    private final Timer timer;
    
    public PedidosMetricas(MeterRegistry registry) {
        this.contador = Counter.builder("gdpd.pedidos.creados")
            .register(registry);
        this.timer = Timer.builder("gdpd.pedidos.tiempo")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);
    }
}
```

### 5. Añadir Dependencias Maven

```xml
<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- JSON Logging -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.3</version>
</dependency>

<!-- Distributed Tracing -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

### 6. Verificar Endpoints

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

---

## Configuración por Entorno

Crea `application-{profile}.yml` para sobrescribir valores:

**application-pro.yml**:
```yaml
logging:
  level:
    root: WARN

management:
  endpoint:
    health:
      show-details: when-authorized
```

---

## Checklist

- [ ] Copiar application.yml y logback-spring.xml
- [ ] Implementar HealthIndicator
- [ ] Implementar métricas custom
- [ ] Añadir dependencias Maven
- [ ] Verificar /actuator/health en local
- [ ] Configurar alertas (ver docs/operations.md)

---

**Referencias**: docs/operations.md, Spring Boot Actuator docs
