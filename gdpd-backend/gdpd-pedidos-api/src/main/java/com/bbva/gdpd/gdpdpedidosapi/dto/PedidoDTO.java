package com.bbva.gdpd.gdpdpedidosapi.dto;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import com.bbva.gdpd.gdpdpedidosapi.domain.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoDTO {
    private Long id;
    private String referencia;
    private EstadoPedido estado;
    private BigDecimal importe;
    private LocalDateTime fechaCreacion;

    public static PedidoDTO from(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.id = p.getId(); dto.referencia = p.getReferencia();
        dto.estado = p.getEstado(); dto.importe = p.getImporte();
        dto.fechaCreacion = p.getFechaCreacion();
        return dto;
    }

    public Long getId() { return id; }
    public String getReferencia() { return referencia; }
    public EstadoPedido getEstado() { return estado; }
    public BigDecimal getImporte() { return importe; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
