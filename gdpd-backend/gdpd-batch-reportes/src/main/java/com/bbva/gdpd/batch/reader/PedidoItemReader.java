package com.bbva.gdpd.batch.reader;

import com.bbva.gdpd.batch.model.PedidoResumen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

@Component
public class PedidoItemReader implements ItemReader<PedidoResumen> {

    private static final Logger log = LoggerFactory.getLogger(PedidoItemReader.class);

    private final JdbcTemplate jdbcTemplate;
    private Iterator<PedidoResumen> iterator;

    public PedidoItemReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PedidoResumen read() {
        if (iterator == null) {
            iterator = cargarResumenes().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<PedidoResumen> cargarResumenes() {
        String periodo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("Cargando resúmenes de pedidos para periodo={}", periodo);
        String sql = "SELECT codigo_cliente, SUM(importe) as importe_total, COUNT(*) as total_pedidos " +
                     "FROM pedidos WHERE TO_CHAR(fecha_creacion, 'YYYY-MM') = ? GROUP BY codigo_cliente";
        return jdbcTemplate.query(sql, new Object[]{periodo}, (rs, rowNum) ->
                new PedidoResumen(rs.getString("codigo_cliente"), rs.getBigDecimal("importe_total"),
                        rs.getLong("total_pedidos"), periodo));
    }

    public void reset() {
        iterator = null;
    }
}
