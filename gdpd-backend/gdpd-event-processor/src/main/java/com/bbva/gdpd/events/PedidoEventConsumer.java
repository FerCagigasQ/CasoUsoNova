package com.bbva.gdpd.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);

    @JmsListener(destination = "gdpd.pedidos.eventos")
    public void onPedidoEvent(String message) {
        log.info("Evento recibido: {}", message);

        if (message.startsWith("PEDIDO_CREADO:")) {
            Long pedidoId = Long.parseLong(message.split(":")[1]);
            procesarPedidoCreado(pedidoId);
        } else if (message.startsWith("PEDIDO_ACTUALIZADO:")) {
            Long pedidoId = Long.parseLong(message.split(":")[1]);
            procesarPedidoActualizado(pedidoId);
        } else if (message.startsWith("PEDIDO_ELIMINADO:")) {
            Long pedidoId = Long.parseLong(message.split(":")[1]);
            procesarPedidoEliminado(pedidoId);
        } else {
            log.warn("Evento desconocido: {}", message);
        }
    }

    private void procesarPedidoCreado(Long pedidoId) {
        log.info("Procesando pedido creado ID={}", pedidoId);
    }

    private void procesarPedidoActualizado(Long pedidoId) {
        log.info("Procesando pedido actualizado ID={}", pedidoId);
    }

    private void procesarPedidoEliminado(Long pedidoId) {
        log.info("Procesando pedido eliminado ID={}", pedidoId);
    }
}
