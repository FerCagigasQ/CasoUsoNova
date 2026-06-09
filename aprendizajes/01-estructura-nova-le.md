# Estructura real de nova-le 7.8.0

> Aprendizaje obtenido del análisis directo del toolchain en `containers/nova-org/toolchain/nova-le/`

## Versión

```json
// version.json (raíz)
{ "version": "7.8.0", "novaVersion": "26.03" }

// tools/version.json
{ "version": "7.8.0" }
```

## Árbol de directorios real

```
nova-le-7.8.0/
├── configuration/               # Templates de configuración de rutas
│   ├── apiconfigtemplate.yml    # Template para rutas de API gateway
│   ├── cdnconfigtemplate.yml    # Template para rutas CDN (Webseal)
│   ├── ces-api.yml              # Config mock CES (autenticación)
│   ├── ces-mocks.yml            # Mocks del CES
│   ├── routes.yml               # Rutas Spring Cloud Gateway (api-gw, config-server, ces-mock)
│   └── websealroutes.yml        # Rutas Webseal mock (frontend CDN + APIs SHIVA)
│
├── generators/                  # Generadores Yeoman para scaffolding
│   ├── generator-nova/          # Servicios backend: API, Batch, Daemon, CDN, etc.
│   │   ├── app/                 # Sub-generador para servicios (create-service)
│   │   │   ├── index.js         # Lógica principal del generador Yeoman
│   │   │   ├── prompts.js       # Preguntas interactivas al usuario
│   │   │   └── constants.js     # Tipos de proyecto, lenguajes, versiones, JDKs
│   │   ├── library/             # Sub-generador para librerías (create-library)
│   │   │   ├── index.js
│   │   │   ├── prompts.js
│   │   │   └── constants.js
│   │   ├── utils.js
│   │   └── package.json
│   ├── generator-thin2/         # Generador proyectos Thin2 (legacy)
│   ├── generator-thin3/         # Generador proyectos Thin3 (Angular 13+)
│   ├── generator-behavior-test/ # Generador tests de lógica de negocio
│   └── generator-asyncapi/      # Generador código AsyncAPI (Broker + ServerPush)
│       ├── generator/           # Motor de generación basado en AsyncAPI oficial v1.7.1
│       └── templates/           # Templates multi-versión (BackToBack, BackToFront)
│
├── nodejs/                      # Node.js embebido
│   ├── node.exe                 # Node.js binary
│   ├── npm.cmd / npm            # npm CLI
│   ├── npx.cmd / npx
│   ├── yarn.cmd / yarn          # Yarn también incluido
│   └── conf/.npmrc              # Configuración npm (posiblemente registry NOVA)
│
├── nova-cli/                    # ⭐ CLI principal
│   ├── bin/                     # Entry points de cada comando
│   │   ├── nova.js              # Punto de entrada principal
│   │   ├── nova-create-service.js
│   │   ├── nova-create-library.js
│   │   ├── nova-create-project.js          # → Thin2
│   │   ├── nova-create-project-Thin3.js    # → Thin3
│   │   ├── nova-create-behavior-test.js
│   │   ├── nova-generate-api-code.js       # Generación REST + AsyncAPI
│   │   ├── nova-runtime-start.js
│   │   ├── nova-runtime-stop.js
│   │   ├── nova-runtime-restart.js
│   │   ├── nova-runtime-status.js
│   │   ├── nova-runtime.js
│   │   ├── nova-config-server.js           # CRUD de propiedades
│   │   ├── nova-config-server-add.js
│   │   ├── nova-config-server-list.js
│   │   ├── nova-config-server-load.js
│   │   ├── nova-config-server-remove.js
│   │   ├── nova-api-gateway.js             # Gestión rutas API Gateway
│   │   ├── nova-api-gateway-add.js
│   │   ├── nova-api-gateway-list.js
│   │   ├── nova-api-gateway-remove.js
│   │   ├── nova-cdn.js                     # Gestión rutas CDN
│   │   ├── nova-cdn-add.js
│   │   ├── nova-cdn-list.js
│   │   ├── nova-cdn-remove.js
│   │   ├── nova-mock-start.js
│   │   ├── nova-mock-stop.js
│   │   ├── nova-mock-list.js
│   │   ├── nova-mock.js
│   │   ├── nova-validate-swagger.js
│   │   ├── nova-validate-batch-scheduler.js
│   │   └── nova-version-check.js
│   ├── src/                     # Lógica de negocio
│   │   ├── nova-cli.js          # Inicialización de entorno (JAVA_HOME, MVN_HOME, etc.)
│   │   ├── constants.js         # 756 líneas: SERVICES, APIGEN_CONFIG, GENERATORS, etc.
│   │   ├── constants-help.js    # Textos de ayuda de cada comando
│   │   ├── generator.service.js # Lógica de generación de código (Yeoman + JAR)
│   │   ├── runtime.service.js   # Start/stop/restart/status de servicios runtime
│   │   ├── process.service.js   # Gestión de procesos del sistema
│   │   ├── configfile.service.js # Lectura/escritura de YAML
│   │   ├── novaconfigserver.service.js # CRUD propiedades Config Server via PostgreSQL
│   │   ├── postgresql.connector.js
│   │   ├── postgresql.custom.js # Init, start, stop PostgreSQL embebido
│   │   ├── activemq.custom.js   # Argumentos JVM para ActiveMQ
│   │   ├── novalocalgw.custom.js # API Gateway local
│   │   ├── novawebsealmock.custom.js # Mock Webseal
│   │   ├── ces-mock.custom.js   # Mock CES (auth)
│   │   ├── mock.service.js      # Mock API server (Swagger → mock)
│   │   ├── version.service.js
│   │   └── logger.service.js
│   └── package.json             # name: "nova-cli", version: "7.8.0"
│
├── nova-le-updater/             # Auto-actualización del toolchain
│   └── package.json
│
├── tools/                       # ⭐ Herramientas embebidas (binarios)
│   ├── java/                    # JDK embebido (Java 8 por defecto)
│   ├── maven/                   # Maven (conf/, lib/, etc.)
│   ├── nova/                    # JARs de infraestructura NOVA
│   │   ├── com.bbva.enoa.discovery-configserver.jar      # Config Server
│   │   ├── com.bbva.enoa.discovery-configserver-j11.jar  # Config Server Java 11
│   │   ├── com.bbva.enoa.discovery-novalocalgw.jar       # API Gateway local
│   │   ├── com.bbva.enoa.discovery-novawebsealmock.jar   # Webseal mock
│   │   ├── com.bbva.enoa.generator-*.jar                 # Generadores API (versiones 1.5.3 → 2.9.2)
│   │   ├── com.bbva.enoa.starter-novastarter.jar         # NOVA Starter
│   │   └── com.bbva.enoa.utils-schedulerparsercli-10.4.0.jar # Parser Batch Scheduler
│   ├── activemq/                # ActiveMQ embebido
│   ├── pgsql/                   # PostgreSQL embebido
│   ├── python/                  # Python 3.7.3 embebido
│   ├── thin2-mocker/            # Mocker para Thin2
│   └── version.json
│
├── start-nova-cmd.bat           # ⭐ Script de arranque (Windows)
└── stop-nova-cmd.bat            # Script de parada
```

## Hallazgo clave: rutas reales vs documentación previa

| Componente | Ruta en nova-cli.js (initializeEnvironment) | Nota |
|------------|---------------------------------------------|------|
| JAVA_HOME | `$NOVA_HOME/tools/java` | NO `$NOVA_HOME/java/` como asumíamos |
| MVN_HOME | `$NOVA_HOME/tools/maven` | NO `$NOVA_HOME/maven/` |
| ACTIVEMQ_HOME | `$NOVA_HOME/tools/activemq` | |
| PYTHON_HOME | `$NOVA_HOME/tools/python` | Python 3.7.3 |
| PostgreSQL | `$NOVA_HOME/tools/pgsql/bin` | Puerto 5555 (no 5432) |
| Node.js | `$NOVA_HOME/nodejs/` | Directamente, no en tools/ |

**Importante**: La inicialización de variables se hace en `nova-cli.js:initializeEnvironment()` y
SOLO se aplica en Windows (`win32`). En `darwin`/`linux` (plataformas excluidas) NO se configuran
automáticamente → el usuario debe configurarlas manualmente.
