# GDPD Operations Guide

Configuración de monitorización, alertas y procedimientos operacionales para la plataforma GDPD en entornos de producción (pro), preproducción (pre) y desarrollo (dev).

## Contenidos

1. [Runtime Local](#runtime-local)
2. [Spring Boot Actuator](#spring-boot-actuator)
3. [Métricas y Monitorización](#métricas-y-monitorización)
4. [Alertas Críticas](#alertas-críticas)
5. [Logging JSON](#logging-json)
6. [Transferencias de Ficheros](#transferencias-de-ficheros)
7. [Procedimiento On-Call](#procedimiento-on-call)
8. [Playbooks de Respuesta a Incidentes](#playbooks-de-respuesta-a-incidentes)

---

## Runtime Local

### Iniciar Todos los Servicios

```bash
nova runtime start all
nova runtime status
```

**Servicios levantados:**

| Servicio | Puerto | URL | Tipo |
|----------|--------|-----|------|
| PostgreSQL | 5555 | jdbc:postgresql://localhost:5555/novadb | Database |
| NOVA Local Gateway | 24000 | http://localhost:24000 | Gateway |
| Config Server | 8888 | http://localhost:8888 | Config |
| NOVA WebSeal Mock | 23000 | https://localhost:23000 | Auth |
| Queue Manager (ActiveMQ) | 8161 | http://localhost:8161 | Broker |
| gdpd-pedidos-api | 8080 | http://localhost:8080 | API |
| gdpd-event-processor | 9080 | http://localhost:9080 | Daemon |

### Verificar Estado

```bash
curl http://localhost:24000/health
curl http://localhost:24000/configserver/health
```

---

## Spring Boot Actuator

Cada servicio expone endpoints de salud y métricas en `/actuator`.

### Endpoints Habilitados

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### Health Check

```bash
# Verificar salud general
curl http://localhost:8080/actuator/health

# JSON estructurado con detalles
curl http://localhost:8080/actuator/health?pretty=true
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "components": {
    "pedidoHealthIndicator": {
      "status": "UP",
      "details": {
        "service": "gdpd-pedidos-api",
        "status": "Operational"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL"
      }
    },
    "rabbit": {
      "status": "UP"
    }
  }
}
```

### Info del Servicio

```bash
curl http://localhost:8080/actuator/info
```

### Métricas de la JVM

```bash
# Consumo de memoria actual
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Heap máximo disponible
curl http://localhost:8080/actuator/metrics/jvm.memory.max

# CPU del sistema
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

---

## Métricas y Monitorización

### Métricas Clave por Servicio

#### gdpd-pedidos-api

**Transacciones:**
- `nova.transacciones.procesadas` (Counter): Total de transacciones exitosas
- `nova.transacciones.fallidas` (Counter): Total de transacciones con error
- `nova.transacciones.tiempo` (Timer): Percentiles P50, P95, P99 de duración

**JVM:**
- `jvm.memory.used`: Memoria heap utilizada (bytes)
- `jvm.memory.max`: Tamaño máximo de heap (bytes)
- `jvm.threads.live`: Threads activos
- `system.cpu.usage`: Porcentaje CPU del sistema

**HTTP:**
- `http.server.requests` (Timer): Latencia de endpoints REST
- `http.server.requests.seconds_count{status=~"5.."}`: Errores 5XX

#### gdpd-event-processor

**Consumo de Eventos:**
- `spring.cloud.stream.binder.rabbit.messages.received`: Total de eventos consumidos
- `spring.cloud.stream.binder.rabbit.messages.failed`: Total de eventos fallidos

**JVM:**
- `jvm.memory.used`: Memoria heap utilizada
- `jvm.threads.live`: Threads activos en el consumer

### Exportación Prometheus

```bash
# Prometheus format (para scraping)
curl http://localhost:8080/actuator/prometheus
```

---

## Alertas Críticas

### Configuración en Portal NOVA

**IMPORTANTE:** Estas alertas deben configurarse en el Portal NOVA → Operaciones → Alertas.

```yaml
# Portal NOVA Alertas Configuration
alertas:
  - nombre: "Servicio Caído - gdpd-pedidos-api"
    servicio: gdpd-pedidos-api
    metrica: /actuator/health
    condicion: "status != UP"
    duracion: 1m
    severidad: CRITICAL
    notificacion:
      - email: oncall-gdpd@bbva.com
      - sms: oncall
      - gira: escalado_automatico

  - nombre: "BD PostgreSQL No Disponible"
    servicio: "*"
    metrica: /actuator/health/db
    condicion: "status != UP"
    duracion: 1m
    severidad: CRITICAL
    notificacion:
      - email: oncall-gdpd@bbva.com
      - dba-team@bbva.com
      - sms: oncall

  - nombre: "Cola RabbitMQ > 1000 Mensajes"
    servicio: gdpd-pedidos-api
    metrica: spring.cloud.stream.binder.rabbit.messages.received
    condicion: "> 1000"
    duracion: 5m
    severidad: HIGH
    notificacion:
      - email: oncall-gdpd@bbva.com
      - middleware-team@bbva.com

  - nombre: "Latencia P95 > 2 segundos"
    servicio: gdpd-pedidos-api
    metrica: http.server.requests.seconds{quantile="0.95"}
    condicion: "> 2.0"
    duracion: 5m
    severidad: WARNING
    notificacion:
      - email: performance-team@bbva.com

  - nombre: "Heap Memory > 80%"
    servicio: "*"
    metrica: "jvm.memory.used / jvm.memory.max"
    condicion: "> 0.80"
    duracion: 10m
    severidad: WARNING
    notificacion:
      - email: oncall-gdpd@bbva.com
      - infra-team@bbva.com

  - nombre: "Error Rate Elevado (> 1%)"
    servicio: gdpd-pedidos-api
    metrica: "http.server.requests.seconds_count{status=~'5..'} / http.server.requests.seconds_count"
    condicion: "> 0.01"
    duracion: 2m
    severidad: HIGH
    notificacion:
      - email: oncall-gdpd@bbva.com
      - sms: oncall

  - nombre: "Consumidor Eventos Atrasado"
    servicio: gdpd-event-processor
    metrica: spring.cloud.stream.binder.rabbit.messages.received
    condicion: "rate < 10 per minute (por > 15min)"
    duracion: 15m
    severidad: MEDIUM
    notificacion:
      - email: middleware-team@bbva.com
```

---

## Logging JSON

### Configuración Logback

Se usa `logstash-logback-encoder` para generar logs en formato JSON en producción (pro/pre).

**Archivo:** `logback-spring.xml`

```xml
<!-- PRODUCCIÓN: JSON para ELK -->
<springProfile name="pro,pre">
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>
                {"app":"${spring.application.name}","env":"${spring.profiles.active}"}
            </customFields>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</springProfile>
```

### Campos Capturados

Cada log en JSON incluye:
- `@timestamp`: ISO 8601
- `@version`: Versión Logstash
- `level`: DEBUG, INFO, WARN, ERROR
- `logger_name`: Clase que genera el log
- `message`: Texto del log
- `app`: Nombre del servicio (ej: gdpd-pedidos-api)
- `env`: Perfil activo (pro, pre, dev, int)
- `trace_id`: Proporcionado por Spring Sleuth (para correlacionar)

### Ejemplo de Log Producción

```json
{
  "@timestamp": "2026-06-09T15:30:45.123Z",
  "level": "INFO",
  "logger_name": "com.bbva.gdpd.pedidos.service.PedidoService",
  "message": "Pedido creado exitosamente",
  "app": "gdpd-pedidos-api",
  "env": "pro",
  "trace_id": "a1b2c3d4e5f6",
  "pedido_id": "PED-2026-001234"
}
```

---

## Transferencias de Ficheros

### Transferencia Entrada: Fichero Diario de Clientes

**Tipo:** ConnectDirect (CD)
**Dirección:** Entrada desde mainframe

```yaml
transferencia:
  nombre: "Fichero Diario Clientes"
  origen:
    nodo: NODO.MAINFRAME.PROD
    fichero: /datos/batch/CLIENTES_YYYYMMDD.csv
    patron: "CLIENTES_*.csv"
  
  destino:
    path: /app/data/input/clientes/
    servidor: gdpd-pedidos-api
  
  planificacion:
    herramienta: Control-M
    cron: "0 6 * * MON-FRI"
    timezone: Europe/Madrid
  
  reintentos: 3
  timeout: 300 segundos
  
  post_transferencia:
    accion: "disparar_batch"
    job: "gdpd-batch-procesarClientes"
    dependencias:
      - validacion_fichero
      - backup_anterior
```

**Monitorización:**
- Verificar completitud: `wc -l /app/data/input/clientes/CLIENTES_*.csv`
- Logs: `docker logs gdpd-batch-reportes | grep "Procesando clientes"`

### Transferencia Salida: Reporte Diario

**Tipo:** Xcom
**Dirección:** Salida hacia distribución

```yaml
transferencia:
  nombre: "Reporte Diario de Pedidos"
  origen:
    path: /app/data/output/reportes/
    patron: "REPORTE_*.pdf"
    servidor: gdpd-batch-reportes
  
  destino:
    nodo: NODO.DISTRIBUCION
    path: /reportes/diarios/
  
  planificacion:
    herramienta: Control-M
    cron: "0 20 * * MON-FRI"
    timezone: Europe/Madrid
  
  compresion: gzip
  notificacion_exito: business-team@bbva.com
  notificacion_error: oncall-gdpd@bbva.com
```

---

## Procedimiento On-Call

### Escalación de Incidentes

**Nivel 1: Alertas Automáticas (Primeros 15 min)**
1. Dashboard Portal NOVA monitorea métricas
2. Alertas CRITICAL disparan SMS + Email automático
3. On-call recibe notificación en el móvil

**Nivel 2: Verificación Manual (15-30 min)**
- On-call accede a Portal NOVA Operaciones
- Verifica: Health check, Logs (ELK), Métricas (Prometheus)
- Si es sencillo → Ejecuta playbook
- Si es complejo → Escala al siguiente nivel

**Nivel 3: Escalado a Especialista (> 30 min)**
- On-call escala a especialista relevante (Backend, DBA, Middleware)
- Crea issue en Jira con contexto inicial

### Gira de On-Call

```
Semana 1 (Jun 9-15):    Equipo A (oncall-gdpd@bbva.com)
Semana 2 (Jun 16-22):   Equipo B
Semana 3 (Jun 23-29):   Equipo C
Semana 4 (Jun 30-Jul6): Equipo D
```

Cada equipo tiene un principal + suplente. Rotación en **Lunes 09:00 CET**.

### Contactos Críticos

| Rol | Email | Teléfono |
|-----|-------|----------|
| On-Call GDPD | oncall-gdpd@bbva.com | +34-91-XXX-XXXX |
| DBA Team | dba-team@bbva.com | +34-91-XXX-XXXX |
| Middleware | middleware-team@bbva.com | +34-91-XXX-XXXX |
| Network/Infra | infra-team@bbva.com | +34-91-XXX-XXXX |

---

## Playbooks de Respuesta a Incidentes

### Incidencia: Servicio No Responde (HTTP 503 / Health DOWN)

**Duración típica:** 5-10 min | **Severidad:** CRITICAL

1. **Verificar Health Check**
   ```bash
   curl http://gdpd-pedidos-api:8080/actuator/health
   # Si status != UP → ir a paso 2
   ```

2. **Revisar Logs Últimos 100 Eventos**
   ```bash
   # Portal NOVA → Operaciones → Logs
   # O vía ELK Kibana para env=pro
   docker logs gdpd-pedidos-api --tail=100
   ```

3. **Verificar Métricas Críticas**
   - CPU: `system.cpu.usage` → Si > 90%, escalar a Infra
   - Memoria: `jvm.memory.used / jvm.memory.max` → Si > 95%, reiniciar
   - Threads: `jvm.threads.live` → Si > 300, investigar memory leak
   - Conexiones BD: `HikariPool.active` → Si == max, pool agotado

4. **Diagnosis por Tipo de Fallo**

   **Si memoria: OOM**
   ```bash
   # Generar heap dump (requiere privilegios)
   curl -O http://gdpd-pedidos-api:8080/actuator/heapdump
   # Enviar a DBA/Performance team para análisis
   ```

   **Si BD no responde:**
   ```bash
   # Verificar conectividad
   psql -h db-pro.nova -U nova -c "SELECT 1"
   # Si falla, contactar DBA team
   ```

   **Si RabbitMQ no disponible:**
   ```bash
   # Verificar broker
   curl http://rabbitmq-cluster.nova:15672/api/vhosts
   # Si falla, contactar Middleware team
   ```

5. **Acciones Correctivas**

   | Problema | Acción | Resultado Esperado |
   |----------|--------|-------------------|
   | Memory leak | Reiniciar pod/contenedor | Health UP en < 1 min |
   | BD desconectada | Reintentos automáticos (Hikari) | Pool reconecta en < 30s |
   | Queue bloqueada | Limpiar cola (Control-M) | Reanudar procesamiento |
   | CPU alta | Escalar réplicas temporalmente | CPU < 70% |

6. **Documentar Incidente**
   ```
   - Hora de detección:
   - Causa raíz:
   - Acción correctiva:
   - Hora de resolución:
   - Impacto: (transacciones perdidas, clientes afectados, duración)
   - Post-mortem: Agendar para 48h después
   ```

---

### Incidencia: Error Rate Elevado (> 1%)

**Duración típica:** 10-15 min | **Severidad:** HIGH

1. **Confirmar Error Rate**
   ```bash
   # Últimas 5 minutos
   curl "http://gdpd-pedidos-api:8080/actuator/metrics/http.server.requests.seconds_count?tag=status:500,status:502,status:503"
   ```

2. **Identificar Endpoint Afectado**
   ```
   Portal NOVA → Métricas → Filtrar por URI
   Buscar qué endpoint tiene mayor ratio de errores
   ```

3. **Revisar Logs de Error**
   ```bash
   # Filtrar por ERROR en últimos 10 minutos
   docker logs gdpd-pedidos-api --since 10m | grep ERROR
   # O en ELK: level:ERROR AND @timestamp:[now-10m TO now]
   ```

4. **Común: Problema en Dependencia**
   - ¿BD rechazando conexiones? → Ver Metrics HikariPool
   - ¿RabbitMQ full? → Ver cola de eventos
   - ¿Timeout en servicio externo? → Ver circuit breaker status

5. **Recuperación**
   - Si es transiente: Esperar + reintentos automáticos
   - Si persiste > 2 min: Escalar a especialista del módulo afectado

---

### Incidencia: Latencia Alta (P95 > 2s)

**Duración típica:** 15-20 min | **Severidad:** MEDIUM/HIGH

1. **Confirmar Latencia**
   ```bash
   curl "http://gdpd-pedidos-api:8080/actuator/metrics/http.server.requests.seconds?tag=quantile:0.95"
   ```

2. **Identificar Endpoint Lento**
   ```
   Portal NOVA → Métricas → http.server.requests
   Filtrar por URI y latencia
   ```

3. **Posibles Causas**
   - Query lenta en BD: Revisar query plan, índices
   - Timeout en RabbitMQ: Revisar tamaño de cola
   - GC pauses: Revisar memoria, gc.pauses metric
   - CPU contention: Escalar réplicas

4. **Acción Correctiva**
   - Escalar réplicas de gdpd-pedidos-api (temporal)
   - Investigar query/índice con DBA si es BD
   - Generar jstack para analizar threads bloqueados

---

### Incidencia: Batch Job Fallido

**Duración típica:** Depende del reintentos | **Severidad:** HIGH

1. **Verificar Estado del Job**
   ```bash
   # Control-M status
   ctm 'status' 'gdpd-batch-procesarClientes'
   
   # Logs del job
   docker logs gdpd-batch-reportes --since 1h | grep gdpd-batch
   ```

2. **Causas Comunes**
   - Fichero de entrada malformado: Validar CSV contra esquema
   - BD bloqueada: Verificar locks con DBA
   - Espacio en disco: Revisar `/app/data/` usage
   - Timeout: Aumentar tiempo límite en Control-M si legítimo

3. **Recuperación**
   - Si es validable: Corregir entrada y reintentar
   - Si es timeout: Aumentar límite temporal y reintentar
   - Si es BD: Esperar a que DBA resuelva, luego reintentar

4. **Notificación**
   - Email automático a business-team@bbva.com
   - Crear issue en Jira si es frecuente

---

## Dashboard Recomendado

**Portal NOVA Operaciones → Crear Dashboard**

Paneles sugeridos:
1. **Estado General** (30s refresh)
   - Health de cada servicio
   - Alertas activas
   - Error count últimos 5 min

2. **Rendimiento** (1 min refresh)
   - CPU + Memoria de cada pod
   - Latencia P95 por endpoint
   - Request rate

3. **Negocio** (5 min refresh)
   - Transacciones procesadas
   - Transacciones fallidas
   - Eventos consumidos (event-processor)

4. **Infraestructura** (2 min refresh)
   - Pool conexiones BD (Hikari)
   - Cola RabbitMQ
   - Threads activos por servicio

---

## Contacto y Escalación

- **Problemas técnicos:** oncall-gdpd@bbva.com
- **Cambios en producción:** Abrir PR en GitHub + Code Review
- **Mejoras operacionales:** Issues en Jira → Epic: GDPD-Operaciones
