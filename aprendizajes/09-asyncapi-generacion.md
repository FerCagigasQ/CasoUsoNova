# AsyncAPI — Generación de código asíncrono

> Fuente: `generators/generator-asyncapi/README.md`, `nova-cli/src/constants.js`

## Arquitectura AsyncAPI en NOVA

NOVA soporta dos patrones de comunicación asíncrona:

### BackToBack (Broker)
- **Patrón**: Service → Broker → Service
- **Brokers**: Kafka o RabbitMQ (default: rabbit)
- **Template Java**: `nova-java-spring-cloud-stream-template`
- **Uso**: Comunicación entre microservicios backend

### BackToFront (ServerPush)
- **Patrón**: Backend → WebSocket/SSE → Frontend Angular
- **Template Java (server)**: `nova-java-serverpush-template`
- **Template Angular (client)**: `nova-angular-serverpush-client-template`
- **Dependencia común Java**: `nova-java-spring-serverpush-common-library`
- **Uso**: Push de eventos en tiempo real al navegador

## Configuración en nova.yml

```yaml
asyncapis:
  backToFront:
    - asyncapi/push-eventos.yml      # Spec AsyncAPI para ServerPush
  backToBack:
    - asyncapi/broker-pedidos.yml     # Spec AsyncAPI para Broker
```

## Generación de código

### Desde CLI usuario

```bash
cd <directorio-con-nova.yml>
nova generate-api-code
# Genera automáticamente tanto APIs REST como AsyncAPI
```

### Desde CLI admin (versión específica)

```bash
nova generate-api-code --async --brokerType kafka -g 0.0.1
```

### Archivos generados

| Archivo | Descripción |
|---------|-------------|
| `ChannelBusinessConfigurator.java` | Configuración de canales Spring Cloud Stream |
| `ImperativePublisher.java` | Publisher imperativo para envío de mensajes |
| `application-local.yml` | Config Spring para desarrollo local |
| `pom.import` | Dependencias Maven a importar en el POM principal |
| `README.md` | Documentación del código generado |

### Directorio de salida

```
asyncapi-generated/
├── backToBack/           # Código para broker (Kafka/Rabbit)
│   └── <nombre>/
└── backToFront/          # Código para ServerPush
    └── serverpush/
        ├── server/       # Java server component
        └── client/       # Angular client component → procesado por prepare-apis-generated.js
```

## Versionado de templates

- Los templates soportan **multi-versión** (conviven varias versiones)
- La relación `novaCliVersion → templateVersion` se define en cada template
- Para usuarios: la versión se infiere del nova.yml
- Para admins NOVA: se especifica explícitamente con `-g <version>`

## Brokers soportados

```javascript
ASYNCAPI: {
  CLIENT: 'client',
  PROVIDER: 'provider',
  KAFKA: 'kafka',
  RABBIT: 'rabbit'        // default
}
```

## Integración con prepare-apis-generated.js

Las librerías Angular generadas para BackToFront/client se procesan por
`prepare-apis-generated.js` (v2.0.1+) que busca en:

```javascript
API_GENERATED_PATHS = [
  'api-generated',
  'asyncapi-generated/backToFront/serverpush/client'  // ← AsyncAPI
];
```

## Patrón Spring Cloud Stream (BackToBack)

El código Java generado usa Spring Cloud Stream para abstracción del broker:
- `@EnableBinding` con canales de input/output
- `ChannelBusinessConfigurator` para configuración de canales
- `ImperativePublisher` para publicación programática
- Profile-based: ActiveMQ en local (`spring.profiles.active=local`), Kafka/Rabbit en producción
