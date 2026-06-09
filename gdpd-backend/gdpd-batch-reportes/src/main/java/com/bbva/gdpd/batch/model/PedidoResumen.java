package com.bbva.gdpd.batch.model;

import java.math.BigDecimal;

public class PedidoResumen {

    private String codigoCliente;
    private BigDecimal importeTotal;
    private Long totalPedidos;
    private String periodo;

    public PedidoResumen() {}

    public PedidoResumen(String codigoCliente, BigDecimal importeTotal, Long totalPedidos, String periodo) {
        this.codigoCliente = codigoCliente;
        this.importeTotal = importeTotal;
        this.totalPedidos = totalPedidos;
        this.periodo = periodo;
    }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String c) { this.codigoCliente = c; }
    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal i) { this.importeTotal = i; }
    public Long getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(Long t) { this.totalPedidos = t; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String p) { this.periodo = p; }
}
