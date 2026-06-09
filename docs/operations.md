# Monitorización y Operaciones — GDPD (Gestión de Pedidos)

> Guía de operaciones, alertas, métricas y procedimientos de respuesta para el producto GDPD en NOVA.

---

## 1. Vista General de Servicios Monitorizados

| Servicio | Tipo | Puerto | Criticidad | Salud | Responsabilidad |
|----------|------|--------|-----------|-------|-----------------|
| `gdpd-pedidos-api` | API REST | 8080 | CRÍTICA | /actuator/health | Endpoints CRUD de pedidos, Swagger |
| `gdpd-event-processor` | Demonio | — | ALTA | /actuator/health | Consumidor de eventos del broker |
| `gdpd-report-batch` | Batch | — | ALTA | /actuator/health | Generación de reportes periódicos |
| `gdpd-report-scheduler` | Scheduler | — | MEDIA | /actuator/health | Orquestación de batches vía cron |

---

## 2. Spring Boot Actuator — Configuración de Health Checks

Cada servicio backend debe exponer los endpoints de Actuator en su `application.yml`:

```yaml
# application.yml (común a todos los servicios backend)
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
  endpoint:
    health:
      show-details: always
info:
  app:
    name: gdpd-pedidos-api
    version: 1.0.0
    uuaa: GDPD
```

### Health Indicators Personalizados por Servicio

#### gdpd-pedidos-api — DatabaseHealthIndicator

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Autowired private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) 
                ? Health.up().withDetail("database", "PostgreSQL").build()
                : Health.down().build();
        } catch (SQLException e) {
            return Health.down().withException(e).build();
        }
    }
}
```

#### gdpd-event-processor — BrokerHealthIndicator

```java
@Component
public class BrokerHealthIndicator implements HealthIndicator {
    @Autowired private JmsTemplate jmsTemplate;
    
    @Override
    public Health health() {
        try {
            jmsTemplate.convertAndSend("health-check", "ping");
            return Health.up().withDetail("broker", "ActiveMQ").build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

---

## 3. Métricas Custom con Micrometer

### gdpd-pedidos-api — Métricas de Pedidos

```java
@Component
public class PedidosMetricas {
    private final Counter pedidosCreados;
    private final Timer tiempoCreacion;
    
    public PedidosMetricas(MeterRegistry registry) {
        this.pedidosCreados = Counter.builder("gdpd.pedidos.creados")
            .tag("servicio", "gdpd-pedidos-api")
            .register(registry);
        
        this.tiempoCreacion = Timer.builder("gdpd.pedidos.tiempo.creacion")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);
    }
    
    public void registrarCreacion(Duration duracion) {
        pedidosCreados.increment();
        tiempoCreacion.record(duracion);
    }
}
```

### gdpd-event-processor — Métricas de Eventos

```java
@Component
public class EventProcessorMetricas {
    private final Counter eventosConsumidos;
    private final Counter eventosProcesados;
    
    public EventProcessorMetricas(MeterRegistry registry) {
        this.eventosConsumidos = Counter.builder("gdpd.eventos.consumidos")
            .register(registry);
        this.eventosProcesados = Counter.builder("gdpd.eventos.procesados")
            .register(registry);
    }
}
```

---

## 4. Logging Estructurado — Logback JSON

Archivo: `logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty name="APP_NAME" source="spring.application.name"/>
    <springProperty name="PROFILE" source="spring.profiles.active" defaultValue="dev"/>

    <!-- PRODUCCIÓN: JSON para ELK -->
    <springProfile name="pro,pre">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <customFields>
                    {"app":"${APP_NAME}","env":"${PROFILE}","uuaa":"GDPD"}
                </customFields>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>

    <!-- DESARROLLO: Legible -->
    <springProfile name="dev,int">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

---

## 5. Alertas Críticas

### Alerta 1: API No Responde (CRÍTICA)

```yaml
alerta:
  nombre: "API REST — Servicio No Responde"
  servicio: gdpd-pedidos-api
  metrica: up{job="gdpd-pedidos-api"}
  condicion: "== 0"
  duracion: 1m
  severidad: CRITICAL
  notificacion:
    - email: equipo-gdpd@bbva.com
    - sms: oncall
```

### Alerta 2: Latencia Elevada (ALTA)

```yaml
alerta:
  nombre: "API REST — Latencia > 2s"
  metrica: http_server_requests_seconds{quantile="0.95"}
  condicion: "> 2"
  duracion: 5m
  severidad: HIGH
```

### Alerta 3: Tasa de Errores (ALTA)

```yaml
alerta:
  nombre: "API REST — Error Rate > 5%"
  metrica: rate(http_server_requests_seconds_count{status=~"5.."}[5m])
  condicion: "> 5"
  duracion: 2m
  severidad: HIGH
```

### Alerta 4: Memoria JVM (ALTA)

```yaml
alerta:
  nombre: "JVM — Heap Memory > 85%"
  metrica: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes
  condicion: "> 0.85"
  duracion: 10m
  severidad: HIGH
```

### Alerta 5: Broker Desconectado (CRÍTICA)

```yaml
alerta:
  nombre: "Demonio — Broker No Accesible"
  servicio: gdpd-event-processor
  metrica: activemq_connection_count
  condicion: "== 0"
  duracion: 1m
  severidad: CRITICAL
  notificacion:
    - email: equipo-middleware@bbva.com
    - sms: oncall
```

### Alerta 6: Lag de Eventos (ALTA)

```yaml
alerta:
  nombre: "Demonio — Eventos en Cola > 1000"
  metrica: activemq_queue_depth{queue="pedidos-events"}
  condicion: "> 1000"
  duracion: 5m
  severidad: HIGH
```

### Alerta 7: Batch Falla (ALTA)

```yaml
alerta:
  nombre: "Batch — Job Fallido"
  metrica: spring_batch_job_status{job="generarReporte"}
  condicion: "== FAILED"
  severidad: HIGH
  notificacion:
    - email: equipo-batch@bbva.com
```

### Alerta 8: Pool de Conexiones (CRÍTICA)

```yaml
alerta:
  nombre: "PostgreSQL — Pool Agotado"
  metrica: hikaricp_connections_max - hikaricp_connections_available
  condicion: "> 95%"
  duracion: 3m
  severidad: CRITICAL
```

### Alerta 9: Config Server Down (CRÍTICA)

```yaml
alerta:
  nombre: "Config Server — No Responde"
  metrica: up{job="config-server"}
  condicion: "== 0"
  duracion: 2m
  severidad: CRITICAL
```

### Alerta 10: Disk Space (ALTA)

```yaml
alerta:
  nombre: "Disk — Espacio < 10%"
  metrica: disk_free_bytes / disk_total_bytes
  condicion: "< 0.10"
  duracion: 5m
  severidad: HIGH
```

---

## 6. Procedimientos de Respuesta a Incidentes

### Incidencia: API No Responde (HTTP 503)

1. **Verificar health**: `curl http://gdpd-pedidos-api:8080/actuator/health`
2. **Revisar logs**: `kubectl logs -f gdpd-pedidos-api --tail=100`
3. **Verificar JVM**:
   - CPU: ¿En 100%? → aumentar replicas
   - Heap: ¿> 80%? → generar heap dump
4. **Verificar BD**: `curl http://gdpd-pedidos-api:8080/actuator/health/db`
5. **Reiniciar** (last resort): `kubectl rollout restart deployment gdpd-pedidos-api`

### Incidencia: Demonio No Procesa Eventos

1. **Ver cola en broker**: Acceder a ActiveMQ console (puerto 8161)
2. **Verificar broker conectado**: `curl http://gdpd-event-processor:8080/actuator/health/broker`
3. **Si DOWN**: Revisar logs del demonio
4. **Si UP pero cola crece**: Aumentar replicas o revisar performance
5. **Monitorizar**: Esperar a que la cola se vacíe

### Incidencia: Batch Job Falla

1. **Revisar logs**: `kubectl logs -f gdpd-report-batch`
2. **Causas comunes**:
   - BD indisponible
   - Datos malformados
   - Timeout
   - OOM
3. **Resolver** según causa y reintentar

---

## 7. Runtime Local — Arranque y Verificación

```bash
# 1. Arrancar todos los servicios
nova runtime start all

# 2. Verificar estado
nova runtime status

# Salida esperada:
# Service              | Status | PID    | Port  | URL
# postgresql           | UP     | 12345  | 5555  | jdbc:postgresql://localhost:5555/gdpddb
# activemq             | UP     | 12346  | 8161  | http://localhost:8161/admin
# api-gateway          | UP     | 12347  | 24000 | http://localhost:24000
# config-server        | UP     | 12348  | 8888  | http://localhost:8888
# webseal-mock         | UP     | 12349  | 23000 | http://localhost:23000
```

### Health Checks Locales

```bash
curl http://localhost:8080/actuator/health      # API
curl http://localhost:9080/actuator/health      # Demonio
curl http://localhost:9081/actuator/health      # Batch
curl http://localhost:9082/actuator/health      # Scheduler
curl http://localhost:8888/actuator/health      # Config Server
```

---

## 8. Transferencias de Ficheros

El batch `gdpd-report-batch` exporta reportes CSV via ConnectDirect/Xcom.

```yaml
app:
  reports:
    output-path: /app/reports/
    pattern: "REPORTE_DIARIO_*.csv"
    compression: gzip
  transfer:
    type: connectdirect  # file, xcom, connectdirect
    schedule: "0 20 * * MON-FRI"  # 20:00 lunes-viernes
    destination:
      path: /reportes/diarios/
      node: NODO.DISTRIBUCION
    retry: 3
    notification:
      on-success: equipo-ops@bbva.com
```

---

## 9. Escalado y Límites de Recursos

| Servicio | CPU | Memoria | Replicas (min/max) |
|----------|-----|---------|-------------------|
| gdpd-pedidos-api | 512m | 1Gi | 2/8 |
| gdpd-event-processor | 256m | 512Mi | 1/4 |
| gdpd-report-batch | 1000m | 2Gi | 1/2 |
| gdpd-report-scheduler | 128m | 256Mi | 1/1 |

```bash
# Horizontal Pod Autoscaler
kubectl autoscale deployment gdpd-pedidos-api --min=2 --max=8 --cpu-percent=70
```

---

## 10. Checklist Pre-Producción

- [ ] Actuator expone endpoints: health, metrics, prometheus
- [ ] Health indicators implementados y funcionales
- [ ] Métricas custom configuradas (Micrometer)
- [ ] Logging JSON en producción (Logback)
- [ ] Spring Cloud Sleuth para tracing distribuido
- [ ] Alertas configuradas en Portal NOVA
- [ ] Dashboards Grafana creados
- [ ] Transferencias de ficheros probadas
- [ ] Runbooks documentados
- [ ] Equipo operaciones entrenado

---

## Referencias

- NOVA Documentation: [docs/arquitectura.md](arquitectura.md)
- Spring Boot Actuator: https://spring.io/guides/gs/actuator-service/
- Micrometer Metrics: https://micrometer.io/docs/concepts
- Spring Cloud Sleuth: https://spring.io/projects/spring-cloud-sleuth
- Logback Configuration: https://logback.qos.ch/manual/configuration.html

---

**Última actualización**: 2026-06-09
**Responsable**: Equipo de Operaciones NOVA
**Versión**: 1.0.0
