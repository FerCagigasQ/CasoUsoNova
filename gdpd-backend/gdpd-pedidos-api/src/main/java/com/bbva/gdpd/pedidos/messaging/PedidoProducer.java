package com.bbva.gdpd.pedidos.messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
@Service
public class PedidoProducer {
    private static final Logger log = LoggerFactory.getLogger(PedidoProducer.class);
    private static final String QUEUE = "pedidos.eventos";
    private final JmsTemplate jmsTemplate;
    public PedidoProducer(JmsTemplate jmsTemplate) { this.jmsTemplate = jmsTemplate; }
    public void publicarEvento(String mensaje) {
        log.info("Publicando en {}: {}", QUEUE, mensaje);
        jmsTemplate.convertAndSend(QUEUE, mensaje);
    }
}
