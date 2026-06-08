# Comunicacion Asincrona GDPD

## Resumen

Arquitectura de mensajeria asincrona del sistema GDPD:
- **Back-to-Back**: Spring Cloud Stream (RabbitMQ) entre gdpd-pedidos-api y gdpd-event-processor
- **Back-to-Front**: Server-Sent Events (SSE) desde gdpd-pedidos-api a gdpd-pedidos-front

---

## Diagrama de flujo de mensajes

```
POST /api/pedidos
      |
      v
+--------------------------------------------------+
|  gdpd-pedidos-api                                |
|  PedidoService.create()                          |
|       |                    |                     |
|       v StreamBridge        v SseEmitter         |
|  PedidoProducer         PedidoSseController      |
+-----|--------------------|-----------------------+
      |                    |
      | pedidos.eventos     | text/event-stream
      v (RabbitMQ)         v /api/pedidos/events
+-------------------+    +------------------------+
|  gdpd-event-      |    | gdpd-pedidos-front     |
|  processor        |    | PedidosListComponent   |
|  Consumer<Pedido  |    | EventSource API        |
|  Event>           |    +------------------------+
+------|------------+
       | (fallo tras 3 reintentos)
       v
  pedidos.eventos.dlq
  pedidosInDlq() -> auditoria
```

---

## Back-to-Back: Spring Cloud Stream

### Canal: pedidos.eventos

**Productor**: gdpd-pedidos-api -> binding pedidos-out-0
**Consumidor**: gdpd-event-processor -> binding pedidosIn-in-0

### AsyncAPI YAML Spec

```yaml
asyncapi: "2.6.0"
info:
  title: GDPD Pedidos Eventos API
  version: "1.0.0"

channels:
  pedidos.eventos:
    description: Canal principal eventos de pedidos (queue point-to-point)
    publish:
      operationId: publishPedidoEvento
      message:
        $ref: "#/components/messages/PedidoEvent"
    subscribe:
      operationId: onPedidoEvento
      message:
        $ref: "#/components/messages/PedidoEvent"

  pedidos.eventos.dlq:
    description: Dead Letter Queue (mensajes fallidos, max 3 reintentos)
    subscribe:
      operationId: onPedidoDLQ
      message:
        $ref: "#/components/messages/PedidoEvent"

components:
  messages:
    PedidoEvent:
      contentType: application/json
      payload:
        $ref: "#/components/schemas/PedidoEvent"
  schemas:
    PedidoEvent:
      type: object
      required: [pedidoId, tipo, estado, origen, timestamp]
      properties:
        pedidoId:      { type: string }
        tipo:          { type: string, enum: [CREACION, ACTUALIZACION_ESTADO, ELIMINACION] }
        estado:        { type: string, enum: [PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO] }
        codigoCliente: { type: string }
        importe:       { type: number }
        timestamp:     { type: string, format: date-time }
        origen:        { type: string }
```

---

## Configuracion DLQ y politica de reintentos

| Parametro | Valor | Descripcion |
|-----------|-------|-------------|
| max-attempts | 3 | Reintentos antes de DLQ |
| back-off-initial-interval | 1000ms | Espera inicial |
| back-off-multiplier | 2.0 | Incremento exponencial |
| back-off-max-interval | 10000ms | Espera maxima |
| requeue-rejected | false | No reencolar rechazados |
| republish-to-dlq | true | Publica en DLQ con metadata |
| dlq-name | pedidos.eventos.dlq | Nombre de la DLQ |

Flujo: Intento 1 -> fallo -> 1s -> Intento 2 -> fallo -> 2s -> Intento 3 -> fallo -> 4s -> DLQ

---

## Back-to-Front: Server-Sent Events (SSE)

### Backend: PedidoSseController.java

```
GET /api/pedidos/events    Content-Type: text/event-stream
```

Cada evento emitido:
```
event: pedido-evento
data: {"pedidoId":"...","tipo":"CREACION","estado":"PENDIENTE",...}
id: <uuid>
retry: 5000
```

### Frontend: PedidosListComponent (Angular)

```typescript
// pedidos-list.component.ts
this.eventSource = new EventSource('/api/pedidos/events');
this.eventSource.addEventListener('pedido-evento', (event: MessageEvent) => {
  const pedidoEvent: PedidoEvent = JSON.parse(event.data);
  this.pedidoEventos = [pedidoEvent, ...this.pedidoEventos];
});
```

EventSource gestiona reconexion automatica segun `retry: 5000` configurado en servidor.

---

## Configuracion Spring Cloud Stream

### gdpd-pedidos-api (productor)

```yaml
spring:
  cloud:
    stream:
      bindings:
        pedidos-out-0:
          destination: pedidos.eventos
          content-type: application/json
      rabbit:
        bindings:
          pedidos-out-0:
            producer:
              auto-bind-dlq: true
              republish-to-dlq: true
```

### gdpd-event-processor (consumidor)

```yaml
spring:
  cloud:
    function:
      definition: pedidosIn;pedidosInDlq
    stream:
      bindings:
        pedidosIn-in-0:
          destination: pedidos.eventos
          group: gdpd-event-processor
          content-type: application/json
        pedidosInDlq-in-0:
          destination: pedidos.eventos.dlq
          group: gdpd-event-processor-dlq
          content-type: application/json
      rabbit:
        bindings:
          pedidosIn-in-0:
            consumer:
              auto-bind-dlq: true
              dlq-name: pedidos.eventos.dlq
              republish-to-dlq: true
              requeue-rejected: false
              max-attempts: 3
              back-off-initial-interval: 1000
              back-off-multiplier: 2.0
              back-off-max-interval: 10000
```

---

## Entornos

| Entorno | Broker | Host |
|---------|--------|------|
| local/dev | RabbitMQ local | localhost:5672 |
| int | RabbitMQ INT | rabbitmq-int.nova.internal |
| pro | RabbitMQ cluster | rabbitmq-cluster.nova.internal |

Credenciales de produccion: variables de entorno RABBIT_USER / RABBIT_PASS.
