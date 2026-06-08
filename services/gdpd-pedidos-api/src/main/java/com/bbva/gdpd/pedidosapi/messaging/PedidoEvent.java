package com.bbva.gdpd.pedidosapi.messaging;

import java.io.Serializable;
import java.time.Instant;

public class PedidoEvent implements Serializable {

    private String eventId;
    private String eventType;   // pedidos.created | pedidos.updated | pedidos.deleted
    private Long pedidoId;
    private String estado;
    private String clienteId;
    private Instant timestamp;
    private String correlationId;

    public PedidoEvent() {}

    public PedidoEvent(String eventType, Long pedidoId, String estado, String clienteId, String correlationId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType;
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.clienteId = clienteId;
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
