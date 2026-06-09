package com.bbva.gdpd.gdpdeventprocessor.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoEventPayload {
    private String eventType;
    private Long pedidoId;
    private String referencia;
    private String estado;
    private BigDecimal importe;
    private LocalDateTime timestamp;

    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long v) { this.pedidoId = v; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { this.referencia = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal v) { this.importe = v; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime v) { this.timestamp = v; }
}
