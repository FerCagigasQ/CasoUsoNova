# Guía de Operaciones — GDPD (Gestión de Pedidos)

## Arranque del sistema

### 1. Prerrequisitos

```bash
# Verificar Java 11 (Zulu JDK)
$NOVA_HOME/tools/java/bin/java -version

# Verificar Maven
$NOVA_HOME/tools/maven/bin/mvn -version

# Verificar Docker
docker --version
```

### 2. Arranque del runtime NOVA (Core)

```bash
# Iniciar servicios de infraestructura
nova runtime start core

# Verificar estado
nova runtime status
```

**Servicios core arrancados:**

| Servicio             | Puerto | Descripción                     |
|----------------------|--------|---------------------------------|
| PostgreSQL           | 5555   | Base de datos principal         |
| Config Server        | 8888   | Configuración centralizada      |
| nova-local-gateway   | 24000  | Gateway / balanceador           |
| nova-webseal-mock    | 23000  | Mock de seguridad WebSEAL       |
| Eureka Server        | 8761   | Registro de servicios           |
| ActiveMQ/RabbitMQ    | 61616  | Message broker                  |

### 3. Arranque completo del stack GDPD

```bash
# Arrancar todos los servicios NOVA
nova runtime start all

# Verificar estado global
nova runtime status

# Salida esperada:
# ✅ postgresql         :5555   RUNNING
# ✅ config-server      :8888   RUNNING
# ✅ nova-local-gateway :24000  RUNNING
# ✅ nova-webseal-mock  :23000  RUNNING
# ✅ eureka             :8761   RUNNING
# ✅ activemq           :61616  RUNNING
# ✅ gdpd-pedidos-api   :8080   RUNNING
# ✅ gdpd-event-processor       RUNNING
```

### 4. Verificación de salud post-arranque

```bash
# API principal
curl -s http://localhost:8080/actuator/health | jq .

# Respuesta esperada:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" },
#     "broker": { "status": "UP" },
#     "eureka": { "status": "UP" },
#     "diskSpace": { "status": "UP" }
#   }
# }

# Info del servicio
curl -s http://localhost:8080/actuator/info | jq .

# Métricas (Prometheus format)
curl -s http://localhost:8080/actuator/prometheus
```

---

## Troubleshooting

### Diagnóstico rápido

```bash
# 1. Estado del runtime
nova runtime status

# 2. Health de todos los servicios
for port in 8080 8081 8082; do
  echo "=== Puerto $port ==="
  curl -sf http://localhost:$port/actuator/health || echo "NO RESPONDE"
done

# 3. Logs recientes
nova logs gdpd-pedidos-api --tail 100
nova logs gdpd-event-processor --tail 100

# 4. Uso de recursos
docker stats --no-stream
```

### Problemas frecuentes y soluciones

#### Servicio no arranca — `Connection refused` al broker

**Síntoma:** `gdpd-event-processor` en estado `FAILED`, logs muestran `Could not connect to broker URL`.

**Solución:**
```bash
# Verificar broker
nova runtime status | grep -i broker

# Reiniciar broker
nova runtime restart activemq

# Verificar conexión
curl -sf http://localhost:8161/admin  # ActiveMQ console
```

#### API sin responder — `504 Gateway Timeout`

**Síntoma:** Peticiones a través del gateway devuelven 504.

**Solución:**
```bash
# Verificar registro en Eureka
curl -s http://localhost:8761/eureka/apps/GDPD-PEDIDOS-API | jq .

# Si no está registrado, reiniciar el servicio
nova runtime restart gdpd-pedidos-api

# Verificar config-server
curl -s http://localhost:8888/gdpd-pedidos-api/dev | jq .
```

#### PostgreSQL — `Connection pool exhausted`

**Síntoma:** Logs muestran `HikariPool - Connection is not available, request timed out`.

**Solución:**
```bash
# Ver conexiones activas
psql -h localhost -p 5555 -U gdpd -c "SELECT count(*) FROM pg_stat_activity WHERE datname='gdpd';"

# Aumentar pool en application.yml (temporal)
# spring.datasource.hikari.maximum-pool-size: 20

# Reiniciar con nueva config
nova runtime restart gdpd-pedidos-api
```

#### Config Server no accesible

**Síntoma:** `Could not locate PropertySource: I/O error on GET request for http://localhost:8888`

**Solución:**
```bash
# Reiniciar config server
nova runtime restart config-server

# Verificar
curl -s http://localhost:8888/actuator/health

# Forzar refresh de config en servicios corriendo
curl -X POST http://localhost:8080/actuator/refresh
```

#### OutOfMemoryError en producción

**Síntoma:** El contenedor se reinicia, logs muestran `java.lang.OutOfMemoryError: Java heap space`.

**Solución:**
```bash
# Ver heap dump (si JAVA_OPTS tiene -XX:+HeapDumpOnOutOfMemoryError)
ls -la /tmp/*.hprof

# Analizar con jmap/jhat o subir a SonarQube Memory Analyzer

# Ajustar JVM en nova.yml para el servicio afectado:
# environment:
#   JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"

# Redesplegar
nova deploy --env integrado --version <BUILD_NUMBER>
```

---

## Escalado

### Escalado horizontal (réplicas)

```yaml
# nova.yml — sección de instancias
servicios:
  gdpd-pedidos-api:
    instancias:
      integrado: 1
      pre: 2
      pro: 3
    recursos:
      cpu: 500m
      memoria: 512Mi
```

### Escalado vertical (recursos JVM)

```bash
# Variables de entorno por entorno en Config Server
# gdpd-pedidos-api-pro.yml:
JAVA_OPTS: "-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Procedimiento de escalado en producción

1. Verificar uso actual: `nova runtime status --env pro`
2. Actualizar `nova.yml` con nuevas instancias
3. Crear release en portal NOVA con nueva configuración
4. Promover a PRE primero y verificar
5. Aprobación doble requerida para PRO
6. Monitorizar métricas durante 30 min post-escalado

---

## Mantenimiento programado

### Parada controlada

```bash
# 1. Poner gateway en modo mantenimiento
nova gateway maintenance --enable --message "Mantenimiento programado"

# 2. Esperar drenado de conexiones (60s)
sleep 60

# 3. Parar servicios en orden inverso
nova runtime stop gdpd-pedidos-api
nova runtime stop gdpd-event-processor
nova runtime stop gdpd-batch-reportes
nova runtime stop core

# 4. Realizar mantenimiento...

# 5. Arrancar en orden
nova runtime start core
nova runtime start all
nova gateway maintenance --disable
```

### Backups

```bash
# Backup manual de PostgreSQL
pg_dump -h localhost -p 5555 -U gdpd gdpd > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup automático — configurado en nova.yml bajo recursos.filesystems
```

---

## Contactos de escalación

| Nivel | Responsable       | Cuándo escalar                                     |
|-------|-------------------|----------------------------------------------------|
| L1    | nova-ops-monitor  | Alertas automáticas, primera respuesta             |
| L2    | nova-release-mgr  | Rollback de release, problemas de configuración    |
| L3    | nova-architect    | Decisiones de arquitectura, incidentes P1          |
