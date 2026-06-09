package com.bbva.gdpd.batch.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_pedidos")
public class ReportePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_cliente", nullable = false)
    private String codigoCliente;

    @Column(name = "importe_total", nullable = false)
    private BigDecimal importeTotal;

    @Column(name = "total_pedidos", nullable = false)
    private Long totalPedidos;

    @Column(nullable = false)
    private String periodo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String c) { this.codigoCliente = c; }
    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal i) { this.importeTotal = i; }
    public Long getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(Long t) { this.totalPedidos = t; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String p) { this.periodo = p; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime f) { this.fechaGeneracion = f; }
}
