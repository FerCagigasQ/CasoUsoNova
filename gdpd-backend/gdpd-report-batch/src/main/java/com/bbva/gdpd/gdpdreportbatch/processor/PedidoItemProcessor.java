package com.bbva.gdpd.gdpdreportbatch.processor;

import com.bbva.gdpd.gdpdreportbatch.domain.PedidoRow;
import com.bbva.gdpd.gdpdreportbatch.domain.ReporteRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PedidoItemProcessor implements ItemProcessor<PedidoRow, ReporteRow> {
    private static final Logger LOG = LoggerFactory.getLogger(PedidoItemProcessor.class);

    @Override
    public ReporteRow process(PedidoRow pedido) {
        LOG.debug("Procesando pedido id={} ref={}", pedido.getId(), pedido.getReferencia());
        return new ReporteRow(pedido.getId(), pedido.getReferencia(),
                pedido.getEstado(), pedido.getImporte(), pedido.getFechaCreacion());
    }
}
