# nova.yml — Configuración central de servicios NOVA

> Fuente: `nova-generate-api-code.js`, `generator.service.js`, `novaconfigserver.service.js`, `constants.js`

## Rol del nova.yml

El fichero `nova.yml` es el **descriptor central** de cada servicio NOVA. Se genera durante
`nova create-service` y es el input principal para `nova generate-api-code`.

## Estructura deducida del código fuente

```yaml
service:
  name: <publicName>           # Nombre del servicio
  uuaa: <UUAA>               # Código UUAA (4 letras, uppercase)
  type: <tipo>                # API | Batch | Daemon | CDN | Batch Scheduler
  language: <lenguaje>        # "Java - Spring boot" | "Python - Flask" | "Angular - Thin3" | etc.
  languageVersion: <version>  # "11.0.11" | "1.8.121" | "3.7.3" | "latest"
  novaCliVersion: <version>   # "7.8.0" — determina qué generador JAR usar
  version: <semver>           # Versión del servicio (e.g. "1.0.0")

# APIs REST
apis:
  served:                     # APIs que este servicio expone (solo API/CDN)
    - swagger/mi-api.yml
  consumed:                   # APIs de otros servicios que este consume
    - swagger/api-externa.yml
  consumedExternal:           # APIs externas (fuera NOVA)
    - swagger/api-terceros.yml

# APIs Asíncronas
asyncapis:
  backToFront:               # ServerPush (WebSocket/SSE)
    - asyncapi/push-eventos.yml
  backToBack:                # Broker (Kafka/Rabbit)
    - asyncapi/broker-pedidos.yml

# Propiedades Config Server
properties:
  - name: mi.propiedad
    description: Descripción de la propiedad
    type: STRING
    default: valor-por-defecto
    management: SERVICE | ENVIRONMENT
    encrypt: false
```

## Cómo usa el CLI el nova.yml

### `nova generate-api-code`

1. Busca `nova.yml` en CWD o ruta proporcionada
2. Lee `service.novaCliVersion` → determina versión del JAR generador
3. Lee `service.languageVersion` → elige entre generador 1.x (Java 8) o 2.x (Java 11)
4. Lee `service.type` + `service.language` → determina qué flavours generar (server/client/feign)
5. Para cada API en `apis.served`, `apis.consumed`, `apis.consumedExternal`:
   - Ejecuta JAR generador con los argumentos correspondientes
6. Para cada AsyncAPI en `asyncapis.backToFront`, `asyncapis.backToBack`:
   - Ejecuta generador AsyncAPI con template correspondiente

### `nova config-server load`

1. Lee propiedades del `nova.yml`
2. Construye `service_name` = `com.bbva.<uuaa>.<name>-releasename-<majorVersion>`
3. Limpia propiedades existentes en PostgreSQL
4. Inserta nuevas propiedades en tabla `configuration_property`

### `nova config-server add/list/remove`

1. Lee/escribe propiedades directamente en el `nova.yml` (sección `properties`)
2. El fichero se actualiza con la nueva propiedad

## Mapeo de versión CLI → versión generador

```javascript
// Últimas versiones relevantes
'7.6.0': { DEFAULT: '1.9.8',  '11.0.11': '2.8.2' },
'7.7.0': { DEFAULT: '1.9.11', '11.0.11': '2.9.2' },
'7.8.0': { DEFAULT: '1.9.11', '11.0.11': '2.9.2' }
```

## Ejemplo de nova.yml completo (inferido)

```yaml
service:
  name: cuentas
  uuaa: ENOA
  type: API
  language: "Java - Spring boot"
  languageVersion: "11.0.11"
  novaCliVersion: "7.8.0"
  version: "1.0.0"

apis:
  served:
    - swagger/cuentas-api.yml
  consumed:
    - swagger/notificaciones-api.yml

properties:
  - name: servicio.cuentas.max-results
    description: Número máximo de resultados por página
    type: STRING
    default: "50"
    management: SERVICE
    encrypt: false
  - name: servicio.cuentas.timeout
    description: Timeout de conexión a servicios externos
    type: STRING
    default: "30000"
    management: ENVIRONMENT
    encrypt: false
```
