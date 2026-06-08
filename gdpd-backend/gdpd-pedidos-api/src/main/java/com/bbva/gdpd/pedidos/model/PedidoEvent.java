package com.bbva.gdpd.pedidos.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PedidoEvent {

    private String pedidoId;
    private String tipo;
    private String estado;
    private String codigoCliente;
    private BigDecimal importe;
    private Instant timestamp;
    private String origen;

    public PedidoEvent() {}

    public PedidoEvent(String pedidoId, String tipo, String estado, String codigoCliente, BigDecimal importe) {
        this.pedidoId = pedidoId;
        this.tipo = tipo;
        this.estado = estado;
        this.codigoCliente = codigoCliente;
        this.importe = importe;
        this.timestamp = Instant.now();
        this.origen = "gdpd-pedidos-api";
    }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}
