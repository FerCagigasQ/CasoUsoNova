package com.gdpd.pedidos.api.client.model;

import java.time.OffsetDateTime;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PedidoDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("numeroPedido")
    private String numeroPedido;

    @SerializedName("clienteId")
    private String clienteId;

    @SerializedName("estado")
    private EstadoPedido estado;

    @SerializedName("fechaCreacion")
    private OffsetDateTime fechaCreacion;

    @SerializedName("fechaActualizacion")
    private OffsetDateTime fechaActualizacion;

    @SerializedName("importeTotal")
    private Double importeTotal;

    @SerializedName("lineas")
    private List<LineaPedidoDTO> lineas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Double getImporteTotal() { return importeTotal; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }

    public List<LineaPedidoDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaPedidoDTO> lineas) { this.lineas = lineas; }
}
