# Guía de Monitorización — GDPD (Gestión de Pedidos)

## Arquitectura de observabilidad

```
┌──────────────────────────────────────────────────────────────────┐
│  Servicios GDPD                                                  │
│  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────┐   │
│  │ gdpd-pedidos-api│  │gdpd-event-process│  │gdpd-batch-rep │   │
│  │   :8080/actuator│  │  :8081/actuator  │  │ :8082/actuator│   │
│  └────────┬────────┘  └────────┬─────────┘  └───────┬───────┘   │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │  Micrometer        │  Prometheus scrape  │
            ▼                    ▼                     ▼
     ┌─────────────────────────────────────────────────────┐
     │              Prometheus (:9090)                     │
     └──────────────────────┬──────────────────────────────┘
                            │
                            ▼
                   ┌────────────────┐
                   │ Grafana (:3000) │
                   │  Dashboards    │
                   └────────────────┘
                            │
                            ▼
                   ┌────────────────┐
                   │ AlertManager   │
                   │  (:9093)       │
                   └────────────────┘
```

---

## Spring Boot Actuator — Endpoints

### Configuración en `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers,env
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true
    diskSpace:
      enabled: true
    jms:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active:dev}

info:
  app:
    name: ${spring.application.name}
    version: @project.version@
    build-time: @maven.build.timestamp@
```

### Endpoints disponibles

| Endpoint                       | Descripción                            |
|-------------------------------|----------------------------------------|
| `GET /actuator/health`        | Estado global del servicio             |
| `GET /actuator/health/liveness` | Kubernetes liveness probe            |
| `GET /actuator/health/readiness` | Kubernetes readiness probe          |
| `GET /actuator/info`          | Versión, build-time, metadatos         |
| `GET /actuator/metrics`       | Lista de métricas disponibles          |
| `GET /actuator/metrics/{name}` | Valor de métrica específica           |
| `GET /actuator/prometheus`    | Métricas en formato Prometheus         |
| `GET /actuator/loggers`       | Niveles de log actuales                |
| `POST /actuator/loggers/{name}` | Cambiar nivel de log en caliente     |
| `GET /actuator/env`           | Variables de entorno (autorizado)      |
| `POST /actuator/refresh`      | Recargar configuración de Config Server|

---

## Micrometer — Métricas Custom

### Métricas implementadas en `gdpd-pedidos-api`

```java
// Contador de pedidos creados
@Bean
Counter pedidosCounter(MeterRegistry registry) {
    return Counter.builder("gdpd.pedidos.creados")
        .description("Total de pedidos creados")
        .tag("servicio", "gdpd-pedidos-api")
        .register(registry);
}

// Gauge de pedidos en procesamiento
@Bean
AtomicInteger pedidosEnProceso(MeterRegistry registry) {
    AtomicInteger gauge = new AtomicInteger(0);
    Gauge.builder("gdpd.pedidos.en_proceso", gauge, AtomicInteger::get)
        .description("Pedidos actualmente en procesamiento")
        .register(registry);
    return gauge;
}

// Timer de latencia de creación de pedido
@Bean
Timer latenciaPedido(MeterRegistry registry) {
    return Timer.builder("gdpd.pedidos.latencia")
        .description("Latencia de creación de pedidos")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(registry);
}

// Rate de eventos procesados (DistributionSummary)
@Bean
DistributionSummary eventosRate(MeterRegistry registry) {
    return DistributionSummary.builder("gdpd.eventos.procesados")
        .description("Rate de eventos procesados por el daemon")
        .tag("servicio", "gdpd-event-processor")
        .register(registry);
}
```

### Métricas estándar incluidas automáticamente

| Métrica                              | Descripción                          |
|--------------------------------------|--------------------------------------|
| `jvm.memory.used`                    | Uso de memoria JVM                   |
| `jvm.gc.pause`                       | Pausas de GC                         |
| `jvm.threads.live`                   | Hilos activos                        |
| `process.cpu.usage`                  | CPU del proceso                      |
| `http.server.requests`               | Latencia y conteo HTTP               |
| `hikaricp.connections.active`        | Conexiones BD activas                |
| `hikaricp.connections.pending`       | Conexiones BD en espera              |
| `spring.integration.channels.*`      | Métricas de mensajería               |
| `logback.events`                     | Conteo de eventos de log por nivel   |

---

## Spring Cloud Sleuth — Distributed Tracing

### Configuración en `application.yml`

```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0       # 100% en dev/int; 0.1 (10%) en pro
    propagation:
      type: B3               # Compatible con Zipkin
    web:
      enabled: true
    async:
      enabled: true
    messaging:
      enabled: true          # Trazas en mensajes JMS/RabbitMQ
  zipkin:
    base-url: http://zipkin:9411
    enabled: true
```

### IDs en los logs

Con Sleuth activo, cada log incluye automáticamente:

```
2026-06-05 10:23:45.123  INFO [gdpd-pedidos-api,abc123def456,789abc] 1 --- [nio-8080-exec-1] c.b.gdpd.PedidosController : Pedido creado: 42
                                         ↑ traceId    ↑ spanId
```

### Consulta de trazas (Zipkin UI)

```bash
# Acceder a UI de Zipkin
open http://localhost:9411

# Filtros útiles:
# - serviceName: gdpd-pedidos-api
# - minDuration: 500ms (para encontrar operaciones lentas)
# - Tags: http.status_code=500 (para errores)
```

---

## Alertas configuradas

### Prometheus Alert Rules — `gdpd-alerts.yml`

```yaml
groups:
  - name: gdpd_alerts
    rules:
      # CPU > 80%
      - alert: GdpdCpuAlta
        expr: process_cpu_usage{application=~"gdpd.*"} > 0.80
        for: 2m
        labels:
          severity: warning
          team: gdpd-ops
        annotations:
          summary: "CPU alta en {{ $labels.application }}"
          description: "CPU al {{ $value | humanizePercentage }} (umbral: 80%)"

      # Memoria > 85%
      - alert: GdpdMemoriaAlta
        expr: |
          jvm_memory_used_bytes{application=~"gdpd.*", area="heap"}
          /
          jvm_memory_max_bytes{application=~"gdpd.*", area="heap"}
          > 0.85
        for: 5m
        labels:
          severity: warning
          team: gdpd-ops
        annotations:
          summary: "Memoria heap alta en {{ $labels.application }}"
          description: "Heap al {{ $value | humanizePercentage }} (umbral: 85%)"

      # Errores HTTP 5xx > 5/min
      - alert: GdpdErrores5xx
        expr: |
          rate(http_server_requests_seconds_count{
            application=~"gdpd.*",
            status=~"5.."
          }[1m]) > 5
        for: 1m
        labels:
          severity: critical
          team: gdpd-ops
        annotations:
          summary: "Errores 5xx en {{ $labels.application }}"
          description: "{{ $value | humanize }} errores 5xx/min en {{ $labels.uri }}"

      # Broker desconectado
      - alert: GdpdBrokerDesconectado
        expr: |
          spring_integration_channels_errorRate{application=~"gdpd.*"} > 0
          or
          absent(up{job="gdpd-event-processor"}) == 1
        for: 1m
        labels:
          severity: critical
          team: gdpd-ops
        annotations:
          summary: "Broker desconectado para {{ $labels.application }}"
          description: "No se puede conectar al message broker. Revisar ActiveMQ/RabbitMQ."

      # Servicio caído
      - alert: GdpdServicioCaido
        expr: up{job=~"gdpd.*"} == 0
        for: 30s
        labels:
          severity: critical
          team: gdpd-ops
        annotations:
          summary: "Servicio GDPD caído: {{ $labels.job }}"
          description: "El servicio {{ $labels.job }} no responde al health check."

      # Latencia alta (p99 > 2s)
      - alert: GdpdLatenciaAlta
        expr: |
          histogram_quantile(0.99,
            rate(http_server_requests_seconds_bucket{
              application=~"gdpd.*"
            }[5m])
          ) > 2.0
        for: 5m
        labels:
          severity: warning
          team: gdpd-ops
        annotations:
          summary: "Latencia p99 alta en {{ $labels.application }}"
          description: "P99 = {{ $value | humanizeDuration }} (umbral: 2s)"

      # Pool BD exhausto
      - alert: GdpdPoolBdExhausto
        expr: |
          hikaricp_connections_pending{application=~"gdpd.*"} > 5
        for: 2m
        labels:
          severity: warning
          team: gdpd-ops
        annotations:
          summary: "Pool de conexiones BD saturado en {{ $labels.application }}"
          description: "{{ $value }} conexiones en espera"
```

---

## Dashboards Grafana

### Dashboard: Visión General GDPD

**Panel 1 — Estado de servicios**
```promql
up{job=~"gdpd.*"}
```

**Panel 2 — Pedidos creados (rate 5m)**
```promql
rate(gdpd_pedidos_creados_total[5m])
```

**Panel 3 — Latencia p50/p95/p99**
```promql
histogram_quantile(0.50, rate(http_server_requests_seconds_bucket{application="gdpd-pedidos-api"}[5m]))
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="gdpd-pedidos-api"}[5m]))
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket{application="gdpd-pedidos-api"}[5m]))
```

**Panel 4 — Errores HTTP**
```promql
rate(http_server_requests_seconds_count{application=~"gdpd.*", status=~"5.."}[1m])
```

**Panel 5 — Uso de memoria heap**
```promql
jvm_memory_used_bytes{application=~"gdpd.*", area="heap"}
/ jvm_memory_max_bytes{application=~"gdpd.*", area="heap"}
* 100
```

**Panel 6 — CPU por servicio**
```promql
process_cpu_usage{application=~"gdpd.*"} * 100
```

**Panel 7 — Eventos procesados (rate)**
```promql
rate(gdpd_eventos_procesados_count[1m])
```

**Panel 8 — Conexiones BD activas**
```promql
hikaricp_connections_active{application=~"gdpd.*"}
```

### Dashboard: Distributed Tracing (Zipkin)

- URL: `http://zipkin:9411`
- Filtrar por servicio: `gdpd-pedidos-api`, `gdpd-event-processor`
- Buscar trazas lentas: `minDuration=500ms`
- Buscar errores: tag `error=true`

---

## Runbooks de alerta

### `GdpdCpuAlta` — CPU > 80%

1. `nova runtime status` — verificar todos los servicios arriba
2. `docker stats gdpd-pedidos-api` — ver uso de recursos en tiempo real
3. Buscar operaciones batch o jobs concurrentes inesperados
4. Si persiste >10 min → escalar a nova-ops-monitor L2

### `GdpdMemoriaAlta` — Heap > 85%

1. Revisar logs: `nova logs gdpd-pedidos-api --tail 200 | grep -i "memory\|heap\|gc"`
2. Forzar GC (temporal): `curl -X POST http://localhost:8080/actuator/gc` (si expuesto)
3. Verificar si hay memory leaks en Zipkin (muchas trazas abiertas)
4. Si OOM inminente → reiniciar servicio
5. Escalar heap en `nova.yml` + nueva release

### `GdpdErrores5xx` — Errores HTTP > 5/min

1. Ver trazas de error en Zipkin: tag `http.status_code=500`
2. Revisar logs: `nova logs gdpd-pedidos-api --tail 200 | grep ERROR`
3. Verificar estado de BD y broker (`/actuator/health`)
4. Si es error de configuración → `curl -X POST http://localhost:8080/actuator/refresh`
5. Si persiste → evaluar rollback de release

### `GdpdBrokerDesconectado` — Broker caído

1. `nova runtime status | grep -i broker`
2. `nova runtime restart activemq` (o rabbitmq según config)
3. Verificar que `gdpd-event-processor` se reconecta automáticamente (Spring retry configurado)
4. Revisar mensajes perdidos en DLQ (Dead Letter Queue)
5. Si pérdida de mensajes → escalar a nova-architect para análisis de impacto

---

## Configuración de notificaciones AlertManager

```yaml
# alertmanager.yml
global:
  slack_api_url: 'https://hooks.slack.com/services/GDPD_OPS_CHANNEL'

route:
  group_by: ['alertname', 'application']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: 'gdpd-ops-team'
  routes:
    - match:
        severity: critical
      receiver: 'gdpd-critical'
      repeat_interval: 1h

receivers:
  - name: 'gdpd-ops-team'
    slack_configs:
      - channel: '#gdpd-alertas'
        title: 'Alerta GDPD: {{ .CommonAnnotations.summary }}'
        text: '{{ .CommonAnnotations.description }}'

  - name: 'gdpd-critical'
    slack_configs:
      - channel: '#gdpd-critico'
        title: '🚨 CRÍTICO GDPD: {{ .CommonAnnotations.summary }}'
        text: '{{ .CommonAnnotations.description }}'
    pagerduty_configs:
      - routing_key: 'GDPD_PAGERDUTY_KEY'
        description: '{{ .CommonAnnotations.summary }}'
```
