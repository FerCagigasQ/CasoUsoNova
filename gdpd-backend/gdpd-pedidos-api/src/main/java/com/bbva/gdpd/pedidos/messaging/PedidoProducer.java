package com.bbva.gdpd.pedidos.messaging;

import com.bbva.gdpd.pedidos.model.PedidoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class PedidoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoProducer.class);
    private static final String BINDING = "pedidos-out-0";

    private final StreamBridge streamBridge;

    public PedidoProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publicarEvento(PedidoEvent evento) {
        log.info("Publicando evento pedidoId={} tipo={}", evento.getPedidoId(), evento.getTipo());
        streamBridge.send(BINDING, evento);
    }
}
