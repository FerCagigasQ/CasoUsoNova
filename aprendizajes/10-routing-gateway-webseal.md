# Routing — API Gateway y Webseal Mock

> Fuente: `configuration/routes.yml`, `configuration/websealroutes.yml`, `configuration/apiconfigtemplate.yml`, `configuration/cdnconfigtemplate.yml`

## Arquitectura de routing local

```
  Browser / Postman
       │
       ├──────────────────────────┐
       │                          │
  Webseal Mock (:23000)    API Gateway (:24000)
       │                          │
       ├── /ENOA/com.bbva.* ──►  CDN/Frontend (:4200, :3000)
       ├── /SHIVA/** ──────────►  API Gateway → Services
       └── /SHIVA2/** ─────────►  API Gateway → Services
                                  │
                                  ├── /UUAA/API/VER/** → Service (:8081, etc.)
                                  ├── /configserver/** → Config Server (:8888)
                                  └── /ces/** ─────────► CES Mock (:36000)
```

## API Gateway (Spring Cloud Gateway :24000)

### Rutas por defecto (routes.yml)

| ID | Pattern | Destino | Filtros |
|----|---------|---------|---------|
| `example_api` | `/UUAA/APINAME/APIVERSION/**` | `http://localhost:8081` | RewritePath, JwtToken |
| `thin2_mock` | `/thin2-mock/**` | `http://localhost:4000` | RewritePath, JwtToken |
| `config_server` | `/com.bbva.enoa.discovery-configserver-nova3-1/**` | `http://localhost:8888` | RewritePath, JwtToken |
| `config_server_apigw` | `/configserver/**` | `http://localhost:8888` | RewritePath, JwtToken |
| `ces_mock` | `/ces/**` | `http://localhost:36000` | RewritePath, JwtToken |

### Añadir ruta de API

```bash
nova api-gateway add
# Prompts:
#   Path name: miapi
#   Host:port: http://localhost:37000
#   UUAA code: enoa
#   API name: usersapi
#   API version: 1.0.1
```

Template generada (`apiconfigtemplate.yml`):
```yaml
id: miapi
uri: http://localhost:37000
predicates:
  - Path=/enoa/usersapi/1.0.1/**
filters:
  - RewritePath=/enoa/usersapi/1.0.1/(?<segment>.*), /${segment}
  - JwtToken
```

Acceso: `http://localhost:23000/SHIVA/enoa/usersapi/1.0.1/recurso`

## Webseal Mock (:23000)

### Rutas por defecto (websealroutes.yml)

| ID | Pattern | Destino |
|----|---------|---------|
| `uuaa` | `/ENOA/com.bbva.uuaa-publicname-release-version/**` | `http://localhost:3000` (frontend) |
| `api` | `/SHIVA/**` | `http://localhost:24000` (gateway) |
| `api2` | `/SHIVA2/**` | `http://localhost:24000` (gateway) |

### Añadir ruta CDN (frontend)

```bash
nova cdn add
# Prompts:
#   Path name: mifrontal
#   UUAA code: enoa
#   Service name: myserv
#   Release name: myrelease
#   Service version: 2.1.3
#   Host:port: http://localhost:3000
```

Template generada (`cdnconfigtemplate.yml`):
```yaml
id: mifrontal
uri: http://localhost:3000
predicates:
  - Path=/ENOA/com.bbva.enoa-myserv-myrelease-2/**
filters:
  - RewritePath=/ENOA/com.bbva.enoa-myserv-myrelease-2/(?<segment>.*), /${segment}
```

Acceso: `http://localhost:23000/ENOA/com.bbva.enoa-myserv-myrelease-2/index.html`

## Filtro JwtToken

Todas las rutas del API Gateway incluyen el filtro `JwtToken` que:
- En local (mock): Genera/acepta JWT mock para simular autenticación CES
- En producción: Es manejado por el Webseal real de BBVA

## Patrón de URLs NOVA

```
Webseal → /SHIVA/<uuaa>/<apiname>/<version>/<recurso>   # APIs
Webseal → /ENOA/com.bbva.<uuaa>-<name>-<release>-<ver>/ # CDN/Frontend
Gateway → /<uuaa>/<apiname>/<version>/<recurso>          # Directo al gateway
```

## Nomenclatura NOVA

| Término | Significado | Ejemplo |
|---------|-------------|---------|
| UUAA | Código de aplicación BBVA (4 letras) | `enoa`, `klxy` |
| SHIVA | Nombre del proxy de APIs NOVA | Siempre `/SHIVA/` |
| ENOA | Nombre del proxy de CDN NOVA | Siempre `/ENOA/` |
| Release | Agrupación de servicios para despliegue | `myrelease` |
