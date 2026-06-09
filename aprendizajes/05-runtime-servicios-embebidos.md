# Runtime — Servicios embebidos de nova-le

> Fuente: `nova-cli/src/constants.js` (SERVICES), `runtime.service.js`, `postgresql.custom.js`, `activemq.custom.js`

## Arquitectura del runtime local

```
┌─────────────────────────────────────────────────────────────────┐
│                   nova runtime (local)                          │
│                                                                 │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │PostgreSQL│  │ ActiveMQ     │  │ Nova Local   │             │
│  │  :5555   │  │  :8161 (web) │  │ Gateway      │             │
│  │          │  │              │  │  :24000      │             │
│  │ core=✅  │  │ core=❌      │  │  core=✅     │             │
│  │ order=0  │  │ order=1      │  │  order=2     │             │
│  └──────────┘  └──────────────┘  └──────┬───────┘             │
│                                         │                      │
│  ┌──────────────┐  ┌──────────────┐     │                      │
│  │ Config       │  │ Webseal Mock │     │                      │
│  │ Server       │  │  :23000      │     │                      │
│  │  :8888       │  │  core=✅     │     │                      │
│  │  core=✅     │  │  order=7     │     │ Spring Cloud Gateway │
│  │  order=5     │  └──────────────┘     │ Routes               │
│  └──────────────┘                       │                      │
│                     ┌──────────────┐    │                      │
│                     │ CES Mock     │    │                      │
│                     │  :36000      │    │                      │
│                     │  core=❌     │◄───┘                      │
│                     │  order=8     │                            │
│                     └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

## Tabla de servicios (detalle completo)

| # | Servicio | Puerto | Core | Orden | Tipo | JAR/Ejecutable | URL acceso |
|---|----------|--------|------|-------|------|----------------|------------|
| 1 | `postgresql` | 5555 | ✅ | 0 | custom | pg_ctl (tools/pgsql/) | N/A |
| 2 | `queue-manager` | 8161 | ❌ | 1 | jar | tools/activemq/bin/activemq.jar | N/A |
| 3 | `nova-local-gateway` | 24000 | ✅ | 2 | jar | tools/nova/com.bbva.enoa.discovery-novalocalgw.jar | http://localhost:24000/ |
| 4 | `config-server` | 8888 | ✅ | 5 | jar | tools/nova/com.bbva.enoa.discovery-configserver.jar | via gateway: /com.bbva.enoa.discovery-configserver-nova3-1/ |
| 5 | `nova-webseal-mock` | 23000 | ✅ | 7 | jar | tools/nova/com.bbva.enoa.discovery-novawebsealmock.jar | http://localhost:23000/ |
| 6 | `ces-mock` | 36000 | ❌ | 8 | custom | configuration/ces-api.yml | http://localhost:36000/auth/ |

## PostgreSQL embebido — Detalles

### Inicialización (primera vez)

```bash
# 1. Crear data directory
initdb -U postgres -A trust -D $NOVA_HOME/tools/pgsql/data/ --encoding=UTF8 --lc-collate=en_US.UTF-8 --lc-ctype=en_US.UTF-8

# 2. Arrancar en puerto 5555
pg_ctl -o '"-p 5555"' -D $NOVA_HOME/tools/pgsql/data/ -l $NOVA_HOME/tools/pgsql/log.txt -w start

# 3. Crear usuario 'nova' con password 'nova'
createuser --username=postgres --host=localhost --port=5555 -P -s nova

# 4. Crear bases de datos
createdb --username=postgres --host=localhost --port=5555 nova_batch_admin
createdb --username=postgres --host=localhost --port=5555 nova_configserver

# 5. Cargar modelo de datos
psql --username=postgres --dbname=nova_batch_admin --host=localhost --port=5555 --file=$NOVA_HOME/tools/pgsql/nova_batch_admin.pgsql
psql --username=postgres --dbname=nova_configserver --host=localhost --port=5555 --file=$NOVA_HOME/tools/pgsql/nova_configserver.pgsql
```

### Bases de datos

| Base de datos | Propósito |
|--------------|-----------|
| `nova_configserver` | Almacena propiedades de configuración del Config Server |
| `nova_batch_admin` | Metadatos de ejecución de Spring Batch (job instances, steps, etc.) |

### Credenciales

- **Admin**: `postgres` (sin password, trust auth)
- **Aplicación**: `nova` / `nova` (superuser)

## ActiveMQ — Detalles

### JVM Args

```
-Dcom.sun.management.jmxremote
-Xms1G -Xmx1G
-Djava.util.logging.config.file=logging.properties
-Djava.security.auth.login.config=$NOVA_HOME/tools/activemq/conf/login.config
-Dactivemq.classpath=$NOVA_HOME/tools/activemq/conf
-Dactivemq.home=$NOVA_HOME/tools/activemq
-Dactivemq.base=$NOVA_HOME/tools/activemq
-Dactivemq.conf=$NOVA_HOME/tools/activemq/conf
-Dactivemq.data=$NOVA_HOME/tools/activemq/data
-Djava.io.tmpdir=$NOVA_HOME/tools/activemq/data/tmp
```

### Acceso

- **Broker**: tcp://localhost:61616 (puerto estándar ActiveMQ, no en SERVICES pero es el default)
- **Web Console**: http://localhost:8161
- **Credenciales web**: admin/admin (configuración por defecto ActiveMQ)

## Nova Local Gateway (Spring Cloud Gateway)

### Rutas configuradas por defecto (routes.yml)

```yaml
routes:
  - id: example_api
    uri: http://localhost:8081
    predicates: [Path=/UUAA/APINAME/APIVERSION/**]
    filters: [RewritePath, JwtToken]

  - id: thin2_mock
    uri: http://localhost:4000
    predicates: [Path=/thin2-mock/**]

  - id: config_server
    uri: http://localhost:8888
    predicates: [Path=/com.bbva.enoa.discovery-configserver-nova3-1/**]

  - id: config_server_apigw
    uri: http://localhost:8888
    predicates: [Path=/configserver/**]

  - id: ces_mock
    uri: http://localhost:36000
    predicates: [Path=/ces/**]
```

### Filtros

- **RewritePath**: Reescribe URLs eliminando el prefijo UUAA/API/VERSION
- **JwtToken**: Inyecta/valida token JWT (mock en local)

## Config Server — Detalles

- Spring Cloud Config Server
- Almacena propiedades en PostgreSQL (tabla `configuration_property`)
- Accesible via gateway: `http://localhost:24000/configserver/`
- Directo: `http://localhost:8888/`
- Existe versión Java 11: `com.bbva.enoa.discovery-configserver-j11.jar`

## Webseal Mock

- Simula el Webseal (reverse proxy) de producción BBVA
- Rutas definidas en `websealroutes.yml`
- Pattern: `/ENOA/com.bbva.<uuaa>-<publicname>-<release>-<version>/**`
- Para APIs: Redirige `/SHIVA/**` al gateway local (:24000)

## CES Mock

- Simula el CES (Central de Seguridad) de BBVA
- Proporciona autenticación mock en desarrollo local
- Puerto: 36000
- Endpoint: `/auth/`

## Orden de arranque y dependencias

```
postgresql (0) → queue-manager (1) → nova-local-gateway (2) → ... → config-server (5) → ... → webseal-mock (7) → ces-mock (8)

Intervalo entre arranques: 15 segundos
```

Core services = postgresql + nova-local-gateway + config-server + nova-webseal-mock
