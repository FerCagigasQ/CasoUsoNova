package com.gdpd.pedidos.api.client.model;

import com.google.gson.annotations.SerializedName;

public class CrearLineaRequest {
    @SerializedName("productoId")
    private String productoId;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("cantidad")
    private Integer cantidad;

    @SerializedName("precioUnitario")
    private Double precioUnitario;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
