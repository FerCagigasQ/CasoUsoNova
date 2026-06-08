package com.bbva.gdpd.eventprocessor.messaging;

import com.bbva.gdpd.eventprocessor.sse.SseEmitterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class PedidoEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventSubscriber.class);

    private final SseEmitterRegistry sseRegistry;

    public PedidoEventSubscriber(SseEmitterRegistry sseRegistry) {
        this.sseRegistry = sseRegistry;
    }

    @Bean
    public Consumer<Message<PedidoEvent>> pedidosCreatedConsumer() {
        return message -> handleEvent(message.getPayload());
    }

    @Bean
    public Consumer<Message<PedidoEvent>> pedidosUpdatedConsumer() {
        return message -> handleEvent(message.getPayload());
    }

    @Bean
    public Consumer<Message<PedidoEvent>> pedidosDeletedConsumer() {
        return message -> handleEvent(message.getPayload());
    }

    private void handleEvent(PedidoEvent event) {
        log.info("Received event type={} pedidoId={} correlationId={}",
                 event.getEventType(), event.getPedidoId(), event.getCorrelationId());
        sseRegistry.broadcast(event);
    }
}
