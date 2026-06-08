package com.gdpd.pedidos.api.client.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CrearPedidoRequest {
    @SerializedName("clienteId")
    private String clienteId;

    @SerializedName("observaciones")
    private String observaciones;

    @SerializedName("lineas")
    private List<CrearLineaRequest> lineas;

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<CrearLineaRequest> getLineas() { return lineas; }
    public void setLineas(List<CrearLineaRequest> lineas) { this.lineas = lineas; }
}
