package com.bbva.gdpd.gdpdpedidosapi.dto;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoEventPayload {
    private String eventType;
    private Long pedidoId;
    private String referencia;
    private EstadoPedido estado;
    private BigDecimal importe;
    private LocalDateTime timestamp;

    public PedidoEventPayload() {}
    public PedidoEventPayload(String et, Long pid, String ref, EstadoPedido est, BigDecimal imp) {
        this.eventType = et; this.pedidoId = pid; this.referencia = ref;
        this.estado = est; this.importe = imp; this.timestamp = LocalDateTime.now();
    }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
