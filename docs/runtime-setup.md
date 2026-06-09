# GDPD Runtime Local Setup

Guía completa para levantar el stack completo de GDPD en desarrollo local usando NOVA CLI.

## Requisitos Previos

1. **NOVA Toolchain instalado** (v7.8.0 o superior)
   ```bash
   nova --version
   ```

2. **Git LFS configurado**
   ```bash
   git lfs pull
   source setup-nova.sh
   ```

3. **Docker Desktop ejecutándose** (con al menos 8GB RAM asignados)

4. **Puertos disponibles:** 5555, 24000, 8888, 23000, 8161, 8080, 8082

## Quickstart

### 1. Iniciar Servicios de Infraestructura

```bash
# Iniciar servicios core (PostgreSQL, Gateway, Config Server, WebSeal Mock)
nova runtime start core

# Esperar ~30 segundos a que levanten
nova runtime status
```

**Salida esperada:**
```
Service              Status  PID   Port    URL
------------------------------------------------------
postgresql           UP      1234  5555    jdbc:postgresql://localhost:5555/novadb
nova-local-gateway   UP      5678  24000   http://localhost:24000
config-server        UP      9012  8888    http://localhost:8888
nova-webseal-mock    UP      3456  23000   https://localhost:23000
```

### 2. Iniciar Queue Manager (Opcional, pero recomendado)

```bash
# Agregar ActiveMQ (RabbitMQ alternativo)
nova runtime start all

nova runtime status
```

Ahora verás también:
```
queue-manager        UP      7890  8161    http://localhost:8161/admin (admin/admin)
```

### 3. Compilar Servicios GDPD

```bash
# Desde raíz del repo
cd gdpd-backend/gdpd-pedidos-api
mvn clean install

# En otra terminal
cd gdpd-backend/gdpd-event-processor
mvn clean install
```

### 4. Iniciar Servicios Backend

**Terminal 1: gdpd-pedidos-api**
```bash
cd gdpd-backend/gdpd-pedidos-api
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Esperarás ver:
```
Started GdpdPedidosApiApplication in 5.2 seconds
```

**Terminal 2: gdpd-event-processor**
```bash
cd gdpd-backend/gdpd-event-processor
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Esperarás ver:
```
Started GdpdEventProcessorApplication in 4.8 seconds
```

### 5. Iniciar Frontend (Opcional)

**Terminal 3:**
```bash
cd gdpd-pedidos-front
npm install
npm start
```

Frontend se abrirá en `http://localhost:4200`

## Verificación Completa

### Health Checks

```bash
# API
curl http://localhost:8080/actuator/health

# Event Processor
curl http://localhost:8082/actuator/health

# Config Server
curl http://localhost:8888/health
```

Todos deben retornar:
```json
{"status":"UP"}
```

### Base de Datos

```bash
# Verificar conexión
psql -h localhost -p 5555 -U nova -d novadb -c "SELECT 1"
# Output: 1

# Ver tablas creadas
psql -h localhost -p 5555 -U nova -d novadb -c "\dt"
```

### RabbitMQ / Queue Manager

```bash
# Admin console
open http://localhost:8161/admin  # ActiveMQ
# O para RabbitMQ (si está en uso)
open http://localhost:15672      # guest/guest
```

### Métricas y Monitorización

```bash
# JVM Metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .value

# Request count
curl http://localhost:8080/actuator/metrics/http.server.requests.seconds_count | jq .value

# Prometheus format (para scraping)
curl http://localhost:8080/actuator/prometheus | grep "^nova\|^http\|^jvm"
```

## Parar Servicios

```bash
# Detener runtime NOVA
nova runtime stop

# Matar procesos Spring Boot manualmente
# Ctrl+C en cada terminal
```

## Troubleshooting

### "Port 5555 already in use"
```bash
# Encontrar proceso usando puerto
lsof -i :5555

# O matar
kill -9 <PID>

# Luego reintentar
nova runtime start core
```

### "RabbitMQ connection refused"
```bash
# Verificar que ActiveMQ está levantado
nova runtime status | grep queue-manager

# Si está DOWN, reiniciar
nova runtime stop
nova runtime start all
```

### "PostgreSQL: could not connect"
```bash
# Verificar logs
docker logs postgres

# O reiniciar
nova runtime stop core
nova runtime start core
```

### "Spring Boot aplicación no inicia"
```bash
# Limpiar compilación
mvn clean

# Borrar cache de .m2
rm -rf ~/.m2/repository/com/bbva

# Reintentar instalación
mvn clean install
mvn spring-boot:run
```

### "Logback error: ClassNotFoundException for LogstashEncoder"
```bash
# Asegurarse que logstash-logback-encoder está en pom.xml
# En gdpd-pedidos-api/pom.xml debe existir:
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.3</version>
</dependency>

# Luego: mvn clean install
```

## Puertos y URLs de Referencia

| Servicio | Puerto | URL | Credenciales |
|----------|--------|-----|---------|
| PostgreSQL | 5555 | jdbc:postgresql://localhost:5555/novadb | nova/nova |
| NOVA Gateway | 24000 | http://localhost:24000 | - |
| Config Server | 8888 | http://localhost:8888 | - |
| WebSeal Mock | 23000 | https://localhost:23000 | - |
| ActiveMQ | 8161 | http://localhost:8161/admin | admin/admin |
| gdpd-pedidos-api | 8080 | http://localhost:8080 | - |
| gdpd-event-processor | 8082 | http://localhost:8082 | - |
| Frontend | 4200 | http://localhost:4200 | - |

## Actuator Endpoints Disponibles

Para cada servicio (puerto 8080 para API, 8082 para processor):

```bash
# Health
curl http://localhost:8080/actuator/health

# Info
curl http://localhost:8080/actuator/info

# All metrics available
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/{metric.name}

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## Profiling en Desarrollo

### Habilitar Debug Logs

Agregar a `application.yml` (dev profile):
```yaml
logging:
  level:
    com.bbva.gdpd: DEBUG
    org.springframework: DEBUG
    org.hibernate.SQL: DEBUG
```

### Análisis de Performance

```bash
# Obtener thread dump (para analizar bloqueos)
curl http://localhost:8080/actuator/threaddump > threads.json

# Obtener heap dump (para memory leaks)
curl -O http://localhost:8080/actuator/heapdump

# Analizar con jhat o Eclipse Memory Analyzer
jhat heapdump
```

## Siguiente Paso: Monitorización Remota

Ver [docs/operations.md](operations.md) para:
- Configuración de alertas en Portal NOVA
- Dashboards recomendados
- Playbooks de incidentes
