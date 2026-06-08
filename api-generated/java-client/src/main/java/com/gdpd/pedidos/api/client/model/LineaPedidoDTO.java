package com.gdpd.pedidos.api.client.model;

import com.google.gson.annotations.SerializedName;

public class LineaPedidoDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("productoId")
    private String productoId;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("cantidad")
    private Integer cantidad;

    @SerializedName("precioUnitario")
    private Double precioUnitario;

    @SerializedName("importeLinea")
    private Double importeLinea;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getImporteLinea() { return importeLinea; }
    public void setImporteLinea(Double importeLinea) { this.importeLinea = importeLinea; }
}
