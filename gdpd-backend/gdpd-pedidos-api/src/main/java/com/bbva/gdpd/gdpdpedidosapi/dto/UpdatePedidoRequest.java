package com.bbva.gdpd.gdpdpedidosapi.dto;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import java.math.BigDecimal;

public class UpdatePedidoRequest {
    private EstadoPedido estado;
    private BigDecimal importe;
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
}
