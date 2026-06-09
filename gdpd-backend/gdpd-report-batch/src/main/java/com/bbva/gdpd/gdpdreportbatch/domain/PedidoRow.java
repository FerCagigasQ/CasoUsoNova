package com.bbva.gdpd.gdpdreportbatch.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class PedidoRow {
    @Id private Long id;
    @Column(name = "referencia") private String referencia;
    @Column(name = "estado") private String estado;
    @Column(name = "importe") private BigDecimal importe;
    @Column(name = "fecha_creacion") private LocalDateTime fechaCreacion;

    public Long getId() { return id; }
    public String getReferencia() { return referencia; }
    public String getEstado() { return estado; }
    public BigDecimal getImporte() { return importe; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
