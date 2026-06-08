package com.bbva.gdpd.pedidosapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    static final String CHANNEL_CREATED  = "pedidos-created-out-0";
    static final String CHANNEL_UPDATED  = "pedidos-updated-out-0";
    static final String CHANNEL_DELETED  = "pedidos-deleted-out-0";

    private final StreamBridge streamBridge;

    public PedidoEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishCreated(PedidoEvent event) {
        event.setEventType("pedidos.created");
        send(CHANNEL_CREATED, event);
    }

    public void publishUpdated(PedidoEvent event) {
        event.setEventType("pedidos.updated");
        send(CHANNEL_UPDATED, event);
    }

    public void publishDeleted(PedidoEvent event) {
        event.setEventType("pedidos.deleted");
        send(CHANNEL_DELETED, event);
    }

    private void send(String channel, PedidoEvent event) {
        log.info("Publishing event type={} pedidoId={} correlationId={}",
                 event.getEventType(), event.getPedidoId(), event.getCorrelationId());
        boolean sent = streamBridge.send(channel,
            MessageBuilder.withPayload(event)
                .setHeader("eventType", event.getEventType())
                .setHeader("correlationId", event.getCorrelationId())
                .build());
        if (!sent) {
            log.error("Failed to publish event type={} pedidoId={}", event.getEventType(), event.getPedidoId());
        }
    }
}
