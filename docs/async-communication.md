# Comunicacion Asincrona GDPD - NOV-47

## Arquitectura Back-to-Back y Back-to-Front

```
gdpd-pedidos-api          RabbitMQ              gdpd-event-processor
      |                      |                          |
   [PedidoService]           |                          |
      |--publicarEvento()--->|                          |
      |   pedidosOutput-out-0|                          |
      |                  pedidos-events                 |
      |                      |---pedidosInput-in-0----->|
      |                      |                   [PedidoEventConsumer]
      |                      |                          |
      |          (error x3)  |                          |
      |                 pedidos-events.DLQ              |
      |                      |---pedidosInputDlq-in-0-->|
      |                      |                   [registrarAuditoria]
      |
   [PedidoSseController]
      |--broadcast()--> SseEmitter[] --> Browser EventSource
```

## AsyncAPI 2.6.0 - Especificacion de Canales

Ver especificacion completa en [docs/asyncapi.yaml](asyncapi.yaml).

```yaml
asyncapi: '2.6.0'
info:
  title: GDPD Pedidos Async API
  version: '1.0.0'
  description: Mensajeria asincrona Back-to-Back via RabbitMQ y Back-to-Front via SSE

channels:
  pedidos-events:
    description: Canal principal de eventos de pedidos (queue, durable)
    subscribe:
      operationId: recibirEventoPedido
      bindings:
        amqp:
          cc: ['pedidosInput-in-0']
          ack: true
      message:
        $ref: '#/components/messages/PedidoEvent'
    publish:
      operationId: publicarEventoPedido
      bindings:
        amqp:
          cc: ['pedidosOutput-out-0']
      message:
        $ref: '#/components/messages/PedidoEvent'

  pedidos-events.DLQ:
    description: Dead Letter Queue - mensajes fallidos tras 3 reintentos
    subscribe:
      operationId: recibirEventoDLQ
      bindings:
        amqp:
          cc: ['pedidosInputDlq-in-0']
      message:
        $ref: '#/components/messages/PedidoEvent'

  /api/pedidos/eventos:
    description: Endpoint SSE Back-to-Front para notificaciones en tiempo real
    subscribe:
      operationId: suscribirSSE
      bindings:
        http:
          type: response
      message:
        name: pedido-evento
        payload:
          $ref: '#/components/schemas/PedidoEvent'

components:
  messages:
    PedidoEvent:
      name: PedidoEvent
      contentType: application/json
      payload:
        $ref: '#/components/schemas/PedidoEvent'

  schemas:
    PedidoEvent:
      type: object
      required: [pedidoId, tipo, estado, timestamp, origen]
      properties:
        pedidoId:
          type: string
          description: Identificador unico del pedido
        tipo:
          type: string
          enum: [CREACION, ACTUALIZACION_ESTADO, ELIMINACION]
        estado:
          type: string
          description: Estado actual del pedido
        codigoCliente:
          type: string
          description: Codigo del cliente propietario del pedido
        importe:
          type: number
          format: decimal
          nullable: true
        timestamp:
          type: string
          format: date-time
        origen:
          type: string
          description: Servicio que genero el evento
```

## Politica DLQ

| Parametro | Valor |
|-----------|-------|
| Cola principal | `pedidos-events` |
| Cola DLQ | `pedidos-events.DLQ` |
| max-attempts | 3 |
| back-off-initial-interval | 1000 ms |
| back-off-multiplier | 2.0 |
| back-off-max-interval | 10000 ms |
| Estrategia fallo | `republish-to-dlq: true` |
| requeue-rejected | false |
| Accion en DLQ | Log WARN + auditoria (no reintento automatico) |

## Spring Cloud Stream - Bindings

### Productor (gdpd-pedidos-api)

```yaml
spring:
  cloud:
    stream:
      bindings:
        pedidosOutput-out-0:
          destination: pedidos-events
          content-type: application/json
      rabbit:
        bindings:
          pedidosOutput-out-0:
            producer:
              auto-bind-dlq: true
              republish-to-dlq: true
```

### Consumidor (gdpd-event-processor)

```yaml
spring:
  cloud:
    function:
      definition: pedidosInput;pedidosInputDlq
    stream:
      bindings:
        pedidosInput-in-0:
          destination: pedidos-events
          group: gdpd-event-processor
        pedidosInputDlq-in-0:
          destination: pedidos-events.DLQ
          group: gdpd-event-processor-dlq
      rabbit:
        bindings:
          pedidosInput-in-0:
            consumer:
              auto-bind-dlq: true
              dlq-name: pedidos-events.DLQ
              max-attempts: 3
```

## Back-to-Front SSE

### Endpoint Backend

- **URL**: `GET /api/pedidos/eventos`
- **Content-Type**: `text/event-stream`
- **Evento**: `pedido-evento`
- **Heartbeat**: comment `connected` al conectar
- **Reconexion**: `reconnectTime: 5000ms`

### Angular EventSource

```typescript
this.eventSource = new EventSource('/api/pedidos/eventos');
this.eventSource.addEventListener('pedido-evento', (event: MessageEvent) => {
  const pedidoEvent: PedidoEvent = JSON.parse(event.data);
  // actualizar UI
});
```

## Entornos

| Entorno | Broker | Host | Credenciales |
|---------|--------|------|--------------|
| local | ActiveMQ / RabbitMQ | localhost:5672 | guest/guest |
| dev | RabbitMQ | rabbitmq-dev | via config-server |
| int | RabbitMQ | rabbitmq-int | via config-server |
| pro | RabbitMQ cluster | rabbitmq-cluster.nova.internal | RABBIT_USER / RABBIT_PASS |
