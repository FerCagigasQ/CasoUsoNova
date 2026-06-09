# Comandos NOVA CLI — Referencia real desde el código fuente

> Fuente: `nova-cli/bin/nova.js` (líneas 36-54)

## Lista completa de comandos

```
nova <comando> [opciones]

NOVA: 26.03
Command Line Tool: 7.8.0
```

| Comando | Descripción | Archivo entrada |
|---------|-------------|-----------------|
| `nova create-service` | Scaffold de servicio NOVA: API, Batch, Batch Scheduler, Daemon, CDN | `nova-create-service.js` |
| `nova create-library` | Crear librería NOVA (Java/Python) | `nova-create-library.js` |
| `nova create-project` | Scaffold proyecto no-NOVA: Thin2 o Thin3 | `nova-create-project.js` |
| `nova create-behavior-test` | Scaffold test de lógica de negocio | `nova-create-behavior-test.js` |
| `nova generate-api-code` | Generar código interfaz API (REST + AsyncAPI) desde nova.yml | `nova-generate-api-code.js` |
| `nova runtime start/stop/restart/status` | Gestionar servicios de infraestructura local | `nova-runtime-*.js` |
| `nova config-server add/list/load/remove` | CRUD de propiedades en Config Server | `nova-config-server-*.js` |
| `nova api-gateway add/list/remove` | Gestionar rutas del API Gateway local | `nova-api-gateway-*.js` |
| `nova cdn add/list/remove` | Gestionar rutas CDN (Webseal) | `nova-cdn-*.js` |
| `nova mock start/stop/list` | Lanzar mock REST API desde spec Swagger 2.0 | `nova-mock-*.js` |
| `nova validate-swagger` | Validar fichero Swagger 2.0 | `nova-validate-swagger.js` |
| `nova validate-batch-scheduler` | Validar scheduler.yml y mostrar paths | `nova-validate-batch-scheduler.js` |
| `nova version-check` | Verificar si hay nueva versión publicada | `nova-version-check.js` |

## Detalle: `nova create-service`

Usa Yeoman (generator-nova) para scaffolding interactivo:

### Flujo de prompts

1. **Service name** (`publicName`): solo letras minúsculas y números
2. **UUAA code** (`uuaa`): exactamente 4 caracteres (código de aplicación BBVA)
3. **Service description** (`serviceDescription`)
4. **Project type** (`type`): selección de lista
5. **Language** (depende del tipo seleccionado)
6. **Language version** (depende del lenguaje)
7. **JDK** (solo para Java 11 → Zulu11.48.21 o Amazon Corretto 11.0.11.9.1)

### Tipos de proyecto (`PROYECT_TYPES`)

| Tipo | Lenguajes disponibles | Descripción |
|------|----------------------|-------------|
| **API** | Java - Spring boot, Python - Flask | Servicio REST |
| **Batch** | Java - Spring Batch, Java - Spring Cloud Task, Python | Procesamiento por lotes |
| **Batch Scheduler** | (nativo NOVA) | Orquestador de Batch |
| **Daemon** | Java - Spring boot, Python | Servicio demonio (JMS listeners) |
| **CDN** | Angular - Thin3, Angular - Thin2, Polymer - Cells | Frontend (Content Delivery Network) |
| **Frontcat** | (legacy) | Migración desde frontCIB |

### Versiones de lenguaje

| Lenguaje | Versiones disponibles |
|----------|-----------------------|
| Java - Spring boot | `1.8.121`, `11.0.11` |
| Java - Spring Batch | `1.8.121`, `11.0.11` |
| Java - Spring Cloud Task | `1.8.121`, `11.0.11` |
| Python - Flask | `3.7.3` |
| Python | `3.7.3` |
| Angular - Thin3 | `latest` |
| Angular - Thin2 | `latest` |
| Polymer - Cells | `latest` |

### JDKs disponibles (solo Java)

| Java Version | JDKs disponibles |
|-------------|------------------|
| `1.8.121` | `zulu8.20.0.5` |
| `11.0.11` | `zulu11.48.21`, `amazon-corretto-11.0.11.9.1` |

### Dependencias base generadas (Java)

```
Java 8 (1.8.121):
  com.bbva.enoa.core.base: 1.4.1
  com.bbva.enoa.core.servicesbase: 2.0.1
  com.bbva.enoa.core.novabatchtask: 3.4.0
  com.bbva.enoa.core.novatask: 3.3.0
  com.bbva.enoa.core.webbase: 2.1.0
  com.bbva.enoa.core.clientbasedirect: 1.1.0

Java 11 (11.0.11):
  com.bbva.enoa.core.base: 9.36.2
```

### Variables generadas por el scaffolding

```javascript
answers.groupId = "com.bbva.<uuaa>";           // e.g. com.bbva.enoa
answers.finalName = "com.bbva.<uuaa>-<publicName>";  // e.g. com.bbva.enoa-myservice
answers.novaVersion = "25.0";                   // versión NOVA del generador
answers.novaCliVersion = "7.8.0";               // versión CLI
```

## Detalle: `nova generate-api-code`

### Flujo completo (desde código fuente `nova-generate-api-code.js`)

1. Busca `nova.yml` en el directorio actual (o ruta dada como argumento)
2. Lee la configuración del servicio: tipo, lenguaje, novaCliVersion
3. Determina la versión del generador JAR:
   - Para CLI 7.8.0 + Java DEFAULT → generator `1.9.11`
   - Para CLI 7.8.0 + Java 11.0.11 → generator `2.9.2`
4. Para APIs REST:
   - **served**: genera código servidor (`--flavourType server --flavourCategory spring.nova`)
   - **consumed**: genera código cliente (`--flavourType client --flavourCategory jaxrs.nova`)
   - **consumedExternal**: igual que consumed
   - **client_feign**: genera Feign client (`--flavourType client --flavourCategory feign.nova`)
5. Para AsyncAPI:
   - BackToBack (broker): genera código Kafka/Rabbit
   - BackToFront (server push): genera código push server + cliente Angular
6. Ejecuta el JAR generador con los argumentos
7. Descomprime el ZIP generado
8. Para Java: ejecuta `mvn_install.cmd/sh` y limpia
9. Para CDN/Angular: indica que hay que ejecutar `prepare-apis-generated.js` después

### Tipos de generación por servicio y lenguaje

| Tipo servicio | Lenguaje | served | consumed | consumedExternal | client_feign |
|---------------|----------|--------|----------|------------------|--------------|
| API | Java - Spring boot | ✅ spring.nova | ✅ jaxrs.nova | ✅ jaxrs.nova | ✅ feign.nova |
| API | Python - Flask | ✅ python3 | ✅ python3 | ✅ python3 | ❌ |
| Batch | Java - Spring Batch | ❌ | ✅ jaxrs.nova | ✅ jaxrs.nova | ❌ |
| Batch | Python | ❌ | ✅ python3 | ✅ python3 | ❌ |
| Daemon | Java - Spring boot | ❌ | ✅ jaxrs.nova | ✅ jaxrs.nova | ✅ feign.nova |
| CDN | Angular - Thin3 | ✅ | ✅ | ❌ | ❌ |

### Opciones admin (restringidas)

```
nova generate-api-code --async          # Generación AsyncAPI
nova generate-api-code --client         # Solo cliente
nova generate-api-code --server         # Solo servidor
nova generate-api-code --feign          # Solo Feign client
nova generate-api-code --brokerType     # kafka | rabbit (default: rabbit)
```

## Detalle: `nova runtime`

### Servicios del runtime (CONSTANTS.SERVICES)

| Servicio | Puerto | Orden arranque | Core | JAR/Custom | URL |
|----------|--------|----------------|------|------------|-----|
| `postgresql` | **5555** | 0 | ✅ | custom | N/A |
| `queue-manager` (ActiveMQ) | 8161 | 1 | ❌ | jar (activemq.jar) | N/A |
| `nova-local-gateway` | 24000 | 2 | ✅ | jar (novalocalgw.jar) | `http://localhost:24000/` |
| `config-server` | 8888 | 5 | ✅ | jar (configserver.jar) | `http://localhost:24000/com.bbva.enoa.discovery-configserver-nova3-1/info` |
| `nova-webseal-mock` | 23000 | 7 | ✅ | jar (novawebsealmock.jar) | `http://localhost:23000/` |
| `ces-mock` | 36000 | 8 | ❌ | custom | `http://localhost:36000/auth/` |

### Subcomandos

```bash
nova runtime start all       # Arranca todos los servicios (15s entre cada uno)
nova runtime start core      # Solo servicios core (postgresql, gw, config, webseal)
nova runtime start <nombre>  # Un servicio específico
nova runtime stop all
nova runtime stop core
nova runtime stop <nombre>
nova runtime restart all
nova runtime restart core
nova runtime restart <nombre>
nova runtime status          # Tabla con Service, Status (UP/DOWN), PID, Port, URL
```

### PostgreSQL embebido

- Puerto: **5555** (no 5432 estándar)
- Usuario: `postgres` (inicialización) / `nova` (creado con password `nova`)
- Bases de datos creadas automáticamente: `nova_batch_admin`, `nova_configserver`
- Data folder: `$NOVA_HOME/tools/pgsql/data/`
- Solo se inicializa si `pg_hba.conf` no existe (primera ejecución)

## Detalle: `nova config-server`

### Subcomandos

```bash
nova config-server add       # Añadir propiedad (interactivo)
nova config-server list      # Listar propiedades del nova.yml
nova config-server remove    # Eliminar propiedad
nova config-server load      # Cargar propiedades en PostgreSQL local
```

### Modelo de datos Config Server

Las propiedades se guardan en PostgreSQL (`nova_configserver.configuration_property`):
- `id`, `deployment_id`, `name`, `service_name`, `value`
- `service_name` formato: `com.bbva.<uuaa>.<nombre>-releasename-<majorVersion>`

### Propiedades nova.yml

Cada propiedad tiene:
- `name`: identificador único
- `description`: descripción
- `type`: STRING
- `default`: valor por defecto
- `management`: SERVICE | ENVIRONMENT
- `encrypt`: boolean (encriptar valor)

## Detalle: `nova api-gateway`

### Registrar ruta API

Prompt: Path name, Host:port, UUAA code (4 chars), API name, API version

Template de ruta generada (`apiconfigtemplate.yml`):
```yaml
id: CONFIG_ID
uri: ADDRESS
predicates:
  - Path=/UUAA/APINAME/APIVERSION/**
filters:
  - RewritePath=/UUAA/APINAME/APIVERSION/(?<segment>.*), /${segment}
  - JwtToken
```

Resultado accesible en: `http://localhost:23000/SHIVA/<uuaa>/<apiname>/<version>/**`

## Detalle: `nova cdn`

Para registrar frontal en Webseal mock:
```yaml
id: CONFIG_ID
uri: ADDRESS
predicates:
  - Path=/ENOA/com.bbva.UUAA-ARTIFACT_ID-RELEASE-VERSION/**
filters:
  - RewritePath=/ENOA/com.bbva.UUAA-ARTIFACT_ID-RELEASE-VERSION/(?<segment>.*), /${segment}
```

## Detalle: `nova mock`

Lanza un mock REST API server para un fichero Swagger 2.0:
```bash
nova mock start <swagger.yml>   # Arranca mock server
nova mock stop <nombre>         # Para mock
nova mock list                  # Lista mocks activos con PID, base path, port
```

## Detalle: `nova validate-swagger`

Valida un Swagger 2.0 spec usando el generador API como validador.
Requiere path absoluto al fichero.

## Detalle: `nova validate-batch-scheduler`

Valida `scheduler.yml` y muestra todos los paths posibles del batch scheduler.
Usa el JAR: `com.bbva.enoa.utils-schedulerparsercli-10.4.0.jar`
