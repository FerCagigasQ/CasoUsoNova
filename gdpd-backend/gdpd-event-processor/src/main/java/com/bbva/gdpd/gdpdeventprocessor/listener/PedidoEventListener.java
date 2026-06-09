package com.bbva.gdpd.gdpdeventprocessor.listener;

import com.bbva.gdpd.gdpdeventprocessor.domain.PedidoEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

@Component
public class PedidoEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(PedidoEventListener.class);

    @Bean
    public Consumer<PedidoEventPayload> pedidosEventsConsumer() {
        return event -> {
            LOG.info("[gdpd-event-processor] Evento: type={}, pedidoId={}, estado={}, ref={}",
                    event.getEventType(), event.getPedidoId(), event.getEstado(), event.getReferencia());
            switch (event.getEventType() != null ? event.getEventType() : "") {
                case "PEDIDO_CREADO":
                    LOG.info("[gdpd-event-processor] Procesando creacion pedido id={}", event.getPedidoId());
                    break;
                case "PEDIDO_ACTUALIZADO":
                    LOG.info("[gdpd-event-processor] Procesando actualizacion pedido id={} -> estado={}",
                            event.getPedidoId(), event.getEstado());
                    break;
                default:
                    LOG.warn("[gdpd-event-processor] Tipo de evento desconocido: {}", event.getEventType());
            }
        };
    }
}
