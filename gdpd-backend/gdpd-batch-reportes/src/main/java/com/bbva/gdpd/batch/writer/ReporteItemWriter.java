package com.bbva.gdpd.batch.writer;

import com.bbva.gdpd.batch.model.ReportePedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReporteItemWriter implements ItemWriter<ReportePedido> {

    private static final Logger log = LoggerFactory.getLogger(ReporteItemWriter.class);

    private final JdbcTemplate jdbcTemplate;

    public ReporteItemWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(List<? extends ReportePedido> items) {
        log.info("Escribiendo {} reportes", items.size());
        String sql = "INSERT INTO reporte_pedidos (codigo_cliente, importe_total, total_pedidos, periodo, fecha_generacion) " +
                     "VALUES (?, ?, ?, ?, NOW()) ON CONFLICT DO NOTHING";
        for (ReportePedido r : items) {
            jdbcTemplate.update(sql, r.getCodigoCliente(), r.getImporteTotal(), r.getTotalPedidos(), r.getPeriodo());
            log.debug("Reporte guardado cliente={} periodo={}", r.getCodigoCliente(), r.getPeriodo());
        }
    }
}
