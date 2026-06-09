package com.bbva.gdpd.gdpdreportbatch.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReporteRow {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Long pedidoId;
    private final String referencia;
    private final String estado;
    private final BigDecimal importe;
    private final String fechaCreacion;

    public ReporteRow(Long pedidoId, String referencia, String estado,
                      BigDecimal importe, LocalDateTime fechaCreacion) {
        this.pedidoId = pedidoId; this.referencia = referencia; this.estado = estado;
        this.importe = importe;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion.format(FMT) : "";
    }

    public String toCsvLine() {
        return String.join(",", String.valueOf(pedidoId), referencia, estado,
                String.valueOf(importe), fechaCreacion);
    }

    public Long getPedidoId() { return pedidoId; }
    public String getReferencia() { return referencia; }
    public String getEstado() { return estado; }
    public BigDecimal getImporte() { return importe; }
    public String getFechaCreacion() { return fechaCreacion; }
}
