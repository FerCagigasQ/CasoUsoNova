---
name: Async Messaging for NOVA
slug: nova-async-messaging
description: Configuración de mensajería asíncrona en NOVA — ActiveMQ (local), RabbitMQ (producción), Spring JMS, Spring AMQP, Server-Sent Events (SSE), y los 3 modelos Back-to-Front.
---

# Async Messaging — NOVA

## Brokers en NOVA

| Entorno | Broker | Protocolo | Puerto |
|---------|--------|-----------|--------|
| Local (NOVA Click) | Apache ActiveMQ 5.x | OpenWire (JMS) | 61616 |
| Integrado | ActiveMQ (cluster) | OpenWire (JMS) | 61616 |
| Preproducción | RabbitMQ 3.x | AMQP 0-9-1 | 5672 |
| Producción | RabbitMQ 3.x (cluster) | AMQP 0-9-1 | 5672 |

## Dependencias Maven

```xml
<!-- ActiveMQ (dev/int) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>

<!-- RabbitMQ (pre/pro) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

## Configuración ActiveMQ (dev)

```yaml
spring:
  activemq:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin
    packages:
      trust-all: true
  jms:
    listener:
      concurrency: 3
      max-concurrency: 10
      acknowledge-mode: auto
    template:
      delivery-mode: persistent
      time-to-live: 86400000  # 24h
```

## Configuración RabbitMQ (pro)

```yaml
spring:
  rabbitmq:
    host: rabbitmq-cluster.nova.internal
    port: 5672
    username: ${RABBIT_USER}
    password: ${RABBIT_PASS}
    virtual-host: /nova
    connection-timeout: 5000
    template:
      exchange: nova.exchange.direct
    listener:
      simple:
        concurrency: 5
        max-concurrency: 20
        prefetch: 10
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
```

## Productor JMS

```java
@Service
public class EventoProducer {

    private final JmsTemplate jmsTemplate;

    // Queue (point-to-point)
    public void enviarAQueue(String destino, Object mensaje) {
        jmsTemplate.convertAndSend(destino, mensaje);
    }

    // Con headers
    public void enviarConMetadata(String destino, Object mensaje, String tipo) {
        jmsTemplate.convertAndSend(destino, mensaje, msg -> {
            msg.setStringProperty("tipo", tipo);
            msg.setStringProperty("timestamp", Instant.now().toString());
            return msg;
        });
    }
}
```

## Consumidor JMS (@JmsListener)

```java
@Component
public class EventoConsumer {

    @JmsListener(destination = "cola.eventos", selector = "tipo = 'ALTA'")
    public void procesarAlta(EventoDTO evento) {
        // Procesamiento...
    }

    @JmsListener(destination = "DLQ.cola.eventos")
    public void procesarDLQ(Message message) {
        // Error handling para mensajes fallidos
    }
}
```

## JMS Config (MessageConverter + Factory)

```java
@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory cf) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setConcurrency("3-10");
        factory.setSessionTransacted(true);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setErrorHandler(t -> log.error("JMS Error: {}", t.getMessage()));
        return factory;
    }
}
```

## SSE (Server-Sent Events) — Backend

```java
@RestController
@RequestMapping("/sse")
public class SseController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable String userId) {
        SseEmitter emitter = new SseEmitter(0L);  // Sin timeout
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    public void enviar(String userId, String evento, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name(evento)
                    .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }
}
```

## SSE — Frontend (EventSource)

```typescript
const eventSource = new EventSource('/sse/subscribe/' + userId);

eventSource.addEventListener('notificacion', (event: MessageEvent) => {
    const data = JSON.parse(event.data);
    // Procesar notificación...
});

eventSource.onerror = () => {
    // EventSource reconecta automáticamente
};
```

## 3 Modelos Back-to-Front (B2F)

### Modelo 1: Directo
```
Backend Service → SseEmitter → Browser EventSource
```
Simple, un solo proceso. No escala horizontalmente.

### Modelo 2: Mediado por Broker
```
Backend → Queue → Demonio SSE → SseEmitter → Browser
```
Desacoplado. El demonio SSE es stateful pero el backend es stateless.

### Modelo 3: Híbrido (pub/sub)
```
Backend → Topic → N Instancias Demonio SSE → SseEmitter → Browser
```
Escalable. Cada instancia del demonio mantiene un subset de conexiones.

## Dead Letter Queue (DLQ)

ActiveMQ configura DLQ automáticamente (`DLQ.<queue-name>`). Para RabbitMQ:

```java
@Bean
public Queue colaEventos() {
    return QueueBuilder.durable("cola.eventos")
        .withArgument("x-dead-letter-exchange", "nova.dlx")
        .withArgument("x-dead-letter-routing-key", "cola.eventos.dlq")
        .withArgument("x-message-ttl", 86400000)
        .build();
}
```
