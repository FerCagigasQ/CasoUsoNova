package com.bbva.gdpd.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventProcessorService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessorService.class);

    public void procesarPedidoCreado(Long pedidoId) {
        log.info("Procesando pedido creado: id={}", pedidoId);
    }

    public void procesarPedidoActualizado(Long pedidoId) {
        log.info("Procesando pedido actualizado: id={}", pedidoId);
    }

    public void procesarPedidoEliminado(Long pedidoId) {
        log.info("Procesando pedido eliminado: id={}", pedidoId);
    }
}
