package com.bbva.gdpd.events;

import com.bbva.gdpd.events.model.PedidoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class PedidoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);

    @Bean
    public Consumer<PedidoEvent> pedidosInput() {
        return evento -> {
            log.info("Evento recibido: pedidoId={} tipo={} estado={}",
                     evento.getPedidoId(), evento.getTipo(), evento.getEstado());
            try {
                procesarEvento(evento);
            } catch (IllegalArgumentException e) {
                log.error("Evento invalido descartado: {}", e.getMessage());
            }
        };
    }

    @Bean
    public Consumer<PedidoEvent> pedidosInputDlq() {
        return evento -> {
            log.warn("Mensaje en DLQ pedidos-events.DLQ: {}", evento);
            registrarAuditoria(evento);
        };
    }

    private void procesarEvento(PedidoEvent evento) {
        if (evento.getPedidoId() == null || evento.getPedidoId().isBlank()) {
            throw new IllegalArgumentException("pedidoId requerido");
        }
        switch (evento.getTipo()) {
            case "CREACION":
                log.info("Procesando CREACION pedidoId={}", evento.getPedidoId()); break;
            case "ACTUALIZACION_ESTADO":
                log.info("Procesando ACTUALIZACION_ESTADO pedidoId={} estado={}", evento.getPedidoId(), evento.getEstado()); break;
            case "ELIMINACION":
                log.info("Procesando ELIMINACION pedidoId={}", evento.getPedidoId()); break;
            default:
                log.warn("Tipo de evento desconocido: {}", evento.getTipo());
        }
    }

    private void registrarAuditoria(PedidoEvent evento) {
        log.error("AUDITORIA_DLQ pedidoId={} tipo={} origen={}", evento.getPedidoId(), evento.getTipo(), evento.getOrigen());
    }
}
