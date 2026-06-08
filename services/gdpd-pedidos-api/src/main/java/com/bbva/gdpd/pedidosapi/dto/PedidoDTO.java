package com.bbva.gdpd.pedidosapi.dto;

public class PedidoDTO {

    private Long id;
    private String clienteId;
    private String estado;

    public PedidoDTO() {}

    public PedidoDTO(Long id, String clienteId, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
