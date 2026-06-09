---
name: NOVA CLI Commands Reference
slug: nova-cli-commands
description: Referencia completa de todos los comandos de la NOVA CLI 7.8.0 para generar, validar, ejecutar y gestionar servicios en la plataforma COSMOS/NOVA de BBVA. Basada en el análisis directo del código fuente del toolchain.
---

# NOVA CLI — Referencia de Comandos

## Toolchain (nova-le 7.8.0)

El NOVA CLI se distribuye como un paquete autocontenido llamado **nova-le** (e.g. `nova-le-7.8.0-windows.zip`).
Contiene: Node.js embebido, Java 8, Maven, Python 3.7.3, PostgreSQL, ActiveMQ y JARs de infraestructura NOVA.

**Configuración estándar:**
1. Descomprimir nova-le → se convierte en `$NOVA_HOME`
2. Ejecutar `start-nova-cmd.bat` (Windows) que hace `yarn link` de todos los generadores
3. Para Java 11: sustituir `$NOVA_HOME/tools/java/` por **Zulu JDK 11** (incluido por separado)
4. Variables de entorno clave (se configuran automáticamente en Windows):
   - `JAVA_HOME=$NOVA_HOME/tools/java`
   - `MVN_HOME=$NOVA_HOME/tools/maven`
   - `ACTIVEMQ_HOME=$NOVA_HOME/tools/activemq`
   - `PYTHON_HOME=$NOVA_HOME/tools/python`

Ver skill **nova-toolchain-setup** para detalle completo.

## Lista completa de comandos

| Comando | Descripción |
|---------|-------------|
| `nova create-service` | Scaffold de servicio NOVA (API, Batch, Batch Scheduler, Daemon, CDN, Frontcat) |
| `nova create-library` | Crear librería compartida (Java/Python) |
| `nova create-project` | Scaffold proyecto Thin2 o Thin3 (dentro de un servicio CDN) |
| `nova create-behavior-test` | Scaffold test de lógica de negocio |
| `nova generate-api-code` | Generar código interfaz API (REST + AsyncAPI) desde nova.yml |
| `nova runtime start\|stop\|restart\|status` | Gestionar servicios de infraestructura local |
| `nova config-server add\|list\|load\|remove` | CRUD de propiedades en Config Server |
| `nova api-gateway add\|list\|remove` | Gestionar rutas del API Gateway local |
| `nova cdn add\|list\|remove` | Gestionar rutas CDN (Webseal mock) |
| `nova mock start\|stop\|list` | Lanzar mock REST API desde spec Swagger 2.0 |
| `nova validate-swagger` | Validar fichero Swagger 2.0 (path absoluto) |
| `nova validate-batch-scheduler` | Validar scheduler.yml |
| `nova version-check` | Verificar si hay nueva versión del CLI |

## Generación de servicios (`nova create-service`)

Usa Yeoman (generator-nova) para scaffolding interactivo.

**Prompts:** publicName → UUAA (4 chars) → description → tipo → lenguaje → versión → JDK (Java)

| Tipo | Lenguajes disponibles |
|------|----------------------|
| **API** | Java - Spring boot, Python - Flask |
| **Batch** | Java - Spring Batch, Java - Spring Cloud Task, Python |
| **Batch Scheduler** | Nativo NOVA (sin selección de lenguaje) |
| **Daemon** | Java - Spring boot, Python |
| **CDN** | Angular - Thin3, Angular - Thin2, Polymer - Cells |
| **Frontcat** | Legacy (migración frontCIB) |

### Versiones y JDKs

| Lenguaje | Versiones | JDK disponible |
|----------|-----------|----------------|
| Java | `1.8.121` | `zulu8.20.0.5` |
| Java | `11.0.11` | `zulu11.48.21`, `amazon-corretto-11.0.11.9.1` |
| Python | `3.7.3` | — |
| Angular | `latest` | — |

### Estructura generada (Java)

- `groupId`: `com.bbva.<uuaa>` (e.g. `com.bbva.enoa`)
- `artifactId`: `com.bbva.<uuaa>-<publicName>`
- `nova.yml` con tipo, lenguaje, versión CLI
- Estructura Maven estándar: `src/main/java/`, `src/test/java/`, `pom.xml`
- `.novarc` (fichero de propiedades interno del proyecto NOVA)

### API (Java - Spring boot)

- `pom.xml` con starters: web, data-jpa, cloud-config, actuator
- `Application.java` con `@SpringBootApplication`
- `Controller.java` con `@RestController`
- `swagger/` con spec OpenAPI base
- Dependencias base Java 8: `com.bbva.enoa.core.base:1.4.1`
- Dependencias base Java 11: `com.bbva.enoa.core.base:9.36.2`

### Daemon (Java - Spring boot / Python)

- Servicio event-driven con JMS Listener (Java) o consumer (Python)
- Configuración dual ActiveMQ (dev) / RabbitMQ (pro)
- Sin endpoints HTTP públicos

### Batch (Java - Spring Batch / Spring Cloud Task / Python)

- Spring Batch: Job → Step → ItemReader/Processor/Writer
- Spring Cloud Task: para tareas puntuales
- Python: script batch con gestión de lotes

### CDN (Angular - Thin3 / Thin2 / Polymer - Cells)

- Servicio de frontend estático
- **Requiere**: ejecutar `nova create-project` después para generar el proyecto Angular
- Acceso via Webseal mock: `http://localhost:23000/ENOA/com.bbva.<uuaa>-<name>-<release>-<version>/`

## Generación de código (`nova generate-api-code`)

Genera código de interfaz API REST y AsyncAPI desde la configuración del `nova.yml`.

### Flujo

1. Lee `nova.yml` (directorio actual o path dado)
2. Determina versión del generador JAR:
   - CLI 7.8.0 + Java 8 → generador `1.9.11`
   - CLI 7.8.0 + Java 11 → generador `2.9.2`
3. Ejecuta generación según tipo:

| Tipo servicio | Lenguaje | served (server) | consumed (client) | client_feign |
|---------------|----------|------------------|--------------------|--------------|
| API | Java - Spring boot | `spring.nova` | `jaxrs.nova` | `feign.nova` |
| API | Python - Flask | `python3` | `python3` | — |
| Batch | Java | — | `jaxrs.nova` | — |
| Daemon | Java | — | `jaxrs.nova` | `feign.nova` |
| CDN | Angular | TypeScript | TypeScript | — |

### Post-generación

- **Java**: ejecuta `mvn_install.cmd/sh` para instalar como artefacto Maven local
- **Angular**: genera en `api-generated/` → ejecutar `node prepare-apis-generated.js`
- **Python**: genera en `api-generated/` → añadir a `requirements.in`

### Flujo frontend completo

```bash
# 1. Generar código TypeScript desde Swagger
nova generate-api-code

# 2. Compilar e instalar las librerías Angular generadas
node prepare-apis-generated.js
# → npm install + ng build por cada librería en api-generated/
# → npm install --save <lib>/lib-generated/dist en el proyecto principal
# → También procesa asyncapi-generated/backToFront/serverpush/client
```

**Sin `prepare-apis-generated.js`, Angular no resolverá los imports** de las librerías generadas.

## Infraestructura local (`nova runtime`)

### Servicios disponibles

| Servicio | Puerto | Core | Orden | Tipo |
|----------|--------|------|-------|------|
| `postgresql` | **5555** | ✅ | 0 | custom (pg_ctl) |
| `queue-manager` (ActiveMQ) | 8161 | ❌ | 1 | jar (activemq.jar) |
| `nova-local-gateway` | **24000** | ✅ | 2 | jar (novalocalgw.jar) |
| `config-server` | **8888** | ✅ | 5 | jar (configserver.jar) |
| `nova-webseal-mock` | **23000** | ✅ | 7 | jar (novawebsealmock.jar) |
| `ces-mock` | **36000** | ❌ | 8 | custom (ces-api.yml) |

**Nota importante:** PostgreSQL usa puerto **5555** (no 5432). El intervalo entre arranques es 15 segundos.

### Subcomandos

```bash
nova runtime start all           # Todos los servicios (orden 0→8, 15s entre cada uno)
nova runtime start core          # Solo core: postgresql, gateway, config-server, webseal-mock
nova runtime start <servicio>    # Un servicio específico
nova runtime stop all|core|<srv>
nova runtime restart all|core|<srv>
nova runtime status              # Tabla: Service | Status (UP/DOWN) | PID | Port | URL
```

### PostgreSQL embebido

- Credenciales: `postgres` (admin, trust), `nova`/`nova` (app, superuser)
- BDs auto-creadas: `nova_batch_admin`, `nova_configserver`
- Data: `$NOVA_HOME/tools/pgsql/data/`

## Config Server (`nova config-server`)

```bash
nova config-server add       # Añadir propiedad interactivamente (name, description, management, default, encrypt)
nova config-server list      # Listar propiedades definidas en nova.yml
nova config-server remove    # Eliminar propiedad por nombre
nova config-server load      # Cargar propiedades en PostgreSQL local (tabla configuration_property)
```

Propiedades: `management: SERVICE|ENVIRONMENT`, `type: STRING`, `encrypt: boolean`
Service name en BD: `com.bbva.<uuaa>.<name>-releasename-<majorVersion>`

## API Gateway (`nova api-gateway`)

```bash
nova api-gateway add     # Registrar ruta: path, host:port, UUAA, API name, version
nova api-gateway list    # Listar rutas configuradas
nova api-gateway remove  # Eliminar ruta por ID
```

URL de acceso: `http://localhost:23000/SHIVA/<uuaa>/<apiname>/<version>/<recurso>`

## CDN (`nova cdn`)

```bash
nova cdn add     # Registrar ruta CDN: path, UUAA, service, release, version, host:port
nova cdn list
nova cdn remove
```

URL: `http://localhost:23000/ENOA/com.bbva.<uuaa>-<service>-<release>-<version>/`

## Mock (`nova mock`)

```bash
nova mock start <swagger.yml>    # Arranca mock REST API desde Swagger 2.0
nova mock stop <nombre>          # Para mock server
nova mock list                   # Tabla: Mock | PID | BASE_PATH | Port | URL
```

## Validación

```bash
nova validate-swagger <path-absoluto>    # Valida Swagger 2.0 spec usando el generador API
nova validate-batch-scheduler [path]     # Valida scheduler.yml (busca en CWD si no se indica path)
```

## Herramientas incluidas en nova-le

| Herramienta | Ruta real en nova-le | Puerto | Notas |
|-------------|---------------------|--------|-------|
| Java 8 (default) | `tools/java/` | — | Se sustituye por Zulu JDK 11 |
| Maven | `tools/maven/` | — | |
| Node.js | `nodejs/` | — | Incluye npm + yarn |
| Python 3.7.3 | `tools/python/` | — | |
| PostgreSQL | `tools/pgsql/` | **5555** | BD local; user: `nova`/`nova` |
| ActiveMQ | `tools/activemq/` | 8161 (web) | Broker JMS local |
| API Generators | `tools/nova/com.bbva.enoa.generator-*.jar` | — | Versiones 1.x (Java 8) y 2.x (Java 11) |
| Config Server | `tools/nova/com.bbva.enoa.discovery-configserver.jar` | **8888** | |
| Nova Local Gateway | `tools/nova/com.bbva.enoa.discovery-novalocalgw.jar` | **24000** | Spring Cloud Gateway |
| Webseal Mock | `tools/nova/com.bbva.enoa.discovery-novawebsealmock.jar` | **23000** | |
| Scheduler Parser | `tools/nova/com.bbva.enoa.utils-schedulerparsercli-10.4.0.jar` | — | Validación batch scheduler |
| prepare-apis-generated.js | raíz toolchain (fuera de nova-le/) | — | Indexación librerías Angular |
