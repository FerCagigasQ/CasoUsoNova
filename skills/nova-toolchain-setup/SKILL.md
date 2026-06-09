---
name: NOVA Toolchain Setup
slug: nova-toolchain-setup
description: Instalación, configuración y uso del toolchain nova-le 7.8.0 — estructura real de directorios, variables de entorno, herramientas embebidas, Zulu JDK 11 y troubleshooting. Basado en análisis directo del código fuente.
---

# NOVA Toolchain Setup (nova-le 7.8.0)

## Estructura real de nova-le

```
nova-le-7.8.0/
├── configuration/               # Templates de rutas (api-gateway, CDN, webseal)
│   ├── apiconfigtemplate.yml    # Template ruta API: /UUAA/APINAME/APIVERSION/**
│   ├── cdnconfigtemplate.yml    # Template ruta CDN: /ENOA/com.bbva.UUAA-.../**
│   ├── routes.yml               # Rutas Spring Cloud Gateway por defecto
│   ├── websealroutes.yml        # Rutas Webseal Mock por defecto
│   ├── ces-api.yml              # Config mock CES (autenticación)
│   └── ces-mocks.yml            # Mocks del CES
│
├── generators/                  # Generadores Yeoman
│   ├── generator-nova/          # Servicios: API, Batch, Daemon, CDN
│   │   ├── app/                 # create-service (index.js, prompts.js, constants.js)
│   │   └── library/             # create-library
│   ├── generator-thin3/         # Proyectos Angular Thin3 (v7.5.0, angular13)
│   ├── generator-thin2/         # Proyectos Angular Thin2 (legacy)
│   ├── generator-behavior-test/ # Tests lógica de negocio
│   └── generator-asyncapi/      # Código AsyncAPI (Broker + ServerPush)
│       ├── generator/           # Motor AsyncAPI oficial v1.7.1 (modificado)
│       └── templates/           # Templates multi-versión
│
├── nodejs/                      # Node.js embebido (node, npm, yarn)
├── nova-cli/                    # CLI principal (bin/, src/, package.json)
│   ├── bin/nova.js              # Entry point
│   └── src/                     # Lógica de negocio (13 comandos)
│
├── nova-le-updater/             # Auto-actualización
│
├── tools/                       # ⭐ Herramientas embebidas
│   ├── java/                    # JDK 8 por defecto (sustituir por Zulu 11)
│   ├── maven/                   # Maven
│   ├── python/                  # Python 3.7.3
│   ├── pgsql/                   # PostgreSQL embebido (puerto 5555)
│   ├── activemq/                # ActiveMQ
│   ├── nova/                    # JARs infraestructura:
│   │   ├── com.bbva.enoa.discovery-configserver.jar       # Config Server
│   │   ├── com.bbva.enoa.discovery-configserver-j11.jar   # Config Server Java 11
│   │   ├── com.bbva.enoa.discovery-novalocalgw.jar        # API Gateway (:24000)
│   │   ├── com.bbva.enoa.discovery-novawebsealmock.jar    # Webseal Mock (:23000)
│   │   ├── com.bbva.enoa.generator-*.jar                  # Generadores API (1.x y 2.x)
│   │   └── com.bbva.enoa.utils-schedulerparsercli-10.4.0.jar
│   └── thin2-mocker/
│
├── start-nova-cmd.bat           # Inicialización Windows (yarn link de todos los módulos)
├── stop-nova-cmd.bat            # Parada limpia
└── version.json                 # {"version":"7.8.0","novaVersion":"26.03"}
```

## Variables de entorno (initializeEnvironment)

Configuradas automáticamente en Windows por `nova-cli/src/nova-cli.js`:

```bash
NOVA_HOME=<ruta-a-nova-le>
JAVA_HOME=$NOVA_HOME/tools/java          # ⚠ tools/java, NO java/
MVN_HOME=$NOVA_HOME/tools/maven          # ⚠ tools/maven, NO maven/
ACTIVEMQ_HOME=$NOVA_HOME/tools/activemq
PYTHON_HOME=$NOVA_HOME/tools/python
PATH=$NOVA_HOME/nodejs:$JAVA_HOME/bin:$MVN_HOME/bin:$PYTHON_HOME:$ACTIVEMQ_HOME/bin:$PATH
```

**Nota**: En macOS/Linux (`darwin`/`linux`) las variables NO se configuran automáticamente — el usuario debe exportarlas manualmente.

## Sustitución Zulu JDK 11

El nova-le incluye Java 8 por defecto. Para Java 11:

```bash
# Respaldar Java 8 (opcional)
mv $NOVA_HOME/tools/java $NOVA_HOME/tools/java-8-backup

# Copiar Zulu JDK 11
cp -r zulu-jdk11/ $NOVA_HOME/tools/java/
# → Zulu11.48+21-CA, Java 11.0.11, Windows x86_64
```

Impacto:
- Generador API: usa versión 2.x (2.9.2 para CLI 7.8.0) en lugar de 1.x
- Config Server: usa `discovery-configserver-j11.jar`
- Dependencias base: `com.bbva.enoa.core.base:9.36.2` (vs 1.4.1 en Java 8)

## Inicialización (start-nova-cmd.bat)

Secuencia de arranque:
1. Configura `NOVA_HOME` y `PATH` con Node.js embebido
2. Configura Yarn prefix y cache en `nova-deps/`
3. `yarn link` de cada componente: nova-cli, nova-le-updater, generator-nova, generator-thin2, generator-thin3, generator-asyncapi, generator-behavior-test
4. Copia `.npmrc` a subcarpetas de templates AsyncAPI (necesario para Artifactory NOVA)
5. Opcionalmente ejecuta `nova version-check`

## Servicios del runtime local

```bash
nova runtime start core    # Servicios esenciales
nova runtime start all     # Todos (15s entre cada uno)
nova runtime status        # Tabla de estado
```

| Servicio | Puerto | Core | Descripción |
|----------|--------|------|-------------|
| `postgresql` | **5555** | ✅ | BD local (user: nova/nova, admin: postgres) |
| `queue-manager` | 8161 | ❌ | ActiveMQ (broker JMS, web console) |
| `nova-local-gateway` | **24000** | ✅ | Spring Cloud Gateway local |
| `config-server` | **8888** | ✅ | Spring Cloud Config Server |
| `nova-webseal-mock` | **23000** | ✅ | Mock del Webseal (reverse proxy) BBVA |
| `ces-mock` | **36000** | ❌ | Mock del CES (Central de Seguridad) |

## prepare-apis-generated.js

Script Node.js (v2.0.1) que compila e instala librerías Angular generadas:

```bash
# Ejecutar en raíz del proyecto Angular, DESPUÉS de nova generate-api-code
node prepare-apis-generated.js
```

Flujo:
1. Busca carpetas `api-generated/` y `asyncapi-generated/backToFront/serverpush/client`
2. Instala `generator-lib-commons-angular@^3.0.0`
3. Por cada librería: `npm install` → `ng build` → `npm install --save <lib>/lib-generated/dist`
4. Limpia `node_modules/` y `.angular/` de cada librería

## Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| `nova` no reconocido | No se ejecutó start-nova-cmd.bat | Ejecutar el script de inicio |
| Error yarn link | node_modules desactualizados | `cd <generador> && yarn install && yarn link` |
| PostgreSQL no arranca | Puerto 5555 ocupado | `pg_ctl -D $NOVA_HOME/tools/pgsql/data/ stop` |
| Java version incorrecta | tools/java/ apunta a Java 8 | Sustituir por Zulu JDK 11 |
| `No hay código de API generado` | No existe api-generated/ | Ejecutar `nova generate-api-code` primero |
| Error ng build en prepare-apis | Angular CLI no disponible | Verificar @angular/cli en PATH |
| Config Server no carga propiedades | PostgreSQL no levantado | `nova runtime start postgresql` primero |
| Plataformas excluidas | macOS/Linux sin auto-config | Exportar manualmente JAVA_HOME, MVN_HOME, etc. |
