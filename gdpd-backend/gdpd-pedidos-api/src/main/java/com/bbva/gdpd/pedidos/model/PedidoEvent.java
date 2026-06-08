package com.bbva.gdpd.pedidos.model;

import java.io.Serializable;

public class PedidoEvent implements Serializable {

    private String pedidoId;
    private String tipo;
    private String estado;
    private String clienteId;
    private Double importe;

    public PedidoEvent() {}

    public PedidoEvent(String pedidoId, String tipo, String estado, String clienteId, Double importe) {
        this.pedidoId = pedidoId;
        this.tipo = tipo;
        this.estado = estado;
        this.clienteId = clienteId;
        this.importe = importe;
    }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
}
