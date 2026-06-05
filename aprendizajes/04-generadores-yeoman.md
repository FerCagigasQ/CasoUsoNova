# Generadores Yeoman — Scaffolding NOVA

> Fuente: `nova-le/generators/` y `nova-cli/src/constants.js` (GENERATORS array)

## Registro de generadores

```javascript
// constants.js líneas 614-654
GENERATORS: [
  { generator: 'nova:library',     name: 'library',       type: 'yeoman' },
  { generator: 'behavior-test',    name: 'behavior-test', type: 'yeoman' },
  { generator: 'nova',             name: 'service',       type: 'yeoman' },
  { generator: 'thin2',            name: 'thin2',         type: 'yeoman' },
  { generator: 'thin3',            name: 'thin3',         type: 'yeoman' },
  { generator: 'tools/nova/com.bbva.enoa.generator-{version}.jar',
                                   name: 'api',           type: 'jar'     },
  { generator: 'tools/nova/com.bbva.enoa.utils-schedulerparsercli-10.4.0.jar',
                                   name: 'scheduler',     type: 'jar'     },
  { generator: '@nova-asyncapi',   name: 'asyncapi',      type: 'asyncapi'}
]
```

## generator-nova (servicios backend)

### Tipos y lenguajes (app/constants.js)

```
API       → Java - Spring boot | Python - Flask
Batch     → Java - Spring Batch | Java - Spring Cloud Task | Python
Daemon    → Java - Spring boot | Python
CDN       → Angular - Thin3 | Angular - Thin2 | Polymer - Cells
Batch Scheduler → (nativo, sin selección de lenguaje)
Frontcat  → (legacy, sin selección de lenguaje)
```

### Recursos opcionales

```javascript
RESOURCES: [
  { name: "Add Rabbit/Kafka broker support", value: "Broker" }
]
```

### Estructura de salida (scaffolding)

El generador usa Nunjucks templates y copia recursivamente, reemplazando:
- `publicName` → nombre del servicio
- `uuaa` → código UUAA (lowercase)
- `subsystem` → nombre del subsistema

Genera:
- `pom.xml` con groupId `com.bbva.<uuaa>`, artifactId `com.bbva.<uuaa>-<publicName>`
- `nova.yml` con configuración del servicio
- Estructura Maven estándar `src/main/java/`, `src/test/java/`
- `.novarc` (properties file interno)

### Archivo `.novarc`

Fichero de propiedades generado por el scaffolding que identifica el proyecto NOVA.

## generator-nova:library (librerías)

### Flujo de prompts

1. Library name
2. UUAA code (4 chars)
3. Library description
4. Library type (depende del tipo seleccionado)
5. Language + version

### Tipos de librería

Usa los mismos lenguajes que servicios pero en contexto de librería reusable.

## generator-thin3 (frontales Angular)

- **Versión**: 7.5.0
- **NOVA version**: 25.07
- **Dependencias clave**: yeoman-generator 2.0.5, chalk, fs-extra, rimraf, uuid
- **Keywords**: bbva, thin3, angular13
- **Requisito**: Debe generarse DENTRO de un servicio CDN (`nova create-service` tipo CDN primero)

## generator-thin2 (legacy)

- Similar a thin3 pero para Angular legacy / Thin2 framework
- Todavía soportado pero no recomendado para nuevos proyectos

## generator-behavior-test

- Scaffolding para tests de lógica de negocio
- Solo disponible para Java
- Usa misma estructura de prompts (publicName, uuaa, description)

## generator-asyncapi

### Estructura (del README.md del generador)

| Tipo | Lenguaje | Clase en CLI | Template |
|------|----------|-------------|----------|
| BackToBack (Broker) | Java | broker-generator.js | nova-java-spring-cloud-stream-template |
| BackToFront (ServerPush) | Java (server) | serverpush-server-generator.js | nova-java-serverpush-template |
| Dependencia BackToFront | Java | serverpush-server-generator.js | nova-java-spring-serverpush-common-library |
| BackToFront (client) | Angular | serverpush-client-generator.js | nova-angular-serverpush-client-template |

### Versionado

- **Generator**: Versión en package.json, no soporta multi-versión, requiere `npm publish`
- **Templates**: Soporta multi-versión (se copian y renombran con sufijo de versión)
- Relación `novaCliVersion → templateVersion` definida en cada template

### Brokers soportados

```javascript
ASYNCAPI: {
  KAFKA: 'kafka',
  RABBIT: 'rabbit'   // default
}
```

### Archivos generados por AsyncAPI

- `ChannelBusinessConfigurator.java` — Configuración de canales
- `ImperativePublisher.java` — Publisher imperativo
- `application-local.yml` — Config local para Spring profiles
- `pom.import` — Dependencias Maven a importar
- `README.md` — Documentación del código generado

## Generador de API (JAR)

El generador de código de API es un JAR Java que se ejecuta con argumentos:

```javascript
// Versiones del generador mapeadas por versión de CLI
'7.8.0': { DEFAULT: '1.9.11', '11.0.11': '2.9.2' }
```

### Flavours de generación

| flavourType | flavourCategory | Resultado |
|-------------|-----------------|-----------|
| server | spring.nova | Interfaces Spring (controller) |
| client | jaxrs.nova | JAX-RS client |
| client | feign.nova | Feign client |
| server | python3 | Flask server |
| client | python3 | Python client |

### Directorio de salida

Siempre en `api-generated/` relativo al proyecto:
```javascript
const API_GENERATION_FOLDER = 'api-generated';
```

### Flujo post-generación

1. JAR genera ZIP con el código
2. CLI descomprime en `api-generated/<serviceName>/`
3. Para Java: ejecuta `mvn_install.cmd/sh` dentro de la carpeta, luego elimina la carpeta
4. Para CDN/Angular: la carpeta queda → ejecutar `prepare-apis-generated.js`
5. Para Python: la carpeta queda → añadir a `requirements.in`
