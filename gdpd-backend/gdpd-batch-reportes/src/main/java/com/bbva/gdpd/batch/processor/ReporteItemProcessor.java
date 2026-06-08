package com.bbva.gdpd.batch.processor;

import com.bbva.gdpd.batch.model.PedidoResumen;
import com.bbva.gdpd.batch.model.ReportePedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ReporteItemProcessor implements ItemProcessor<PedidoResumen, ReportePedido> {

    private static final Logger log = LoggerFactory.getLogger(ReporteItemProcessor.class);

    @Override
    public ReportePedido process(PedidoResumen resumen) {
        log.debug("Procesando resumen cliente={} periodo={}", resumen.getCodigoCliente(), resumen.getPeriodo());
        ReportePedido reporte = new ReportePedido();
        reporte.setCodigoCliente(resumen.getCodigoCliente());
        reporte.setImporteTotal(resumen.getImporteTotal());
        reporte.setTotalPedidos(resumen.getTotalPedidos());
        reporte.setPeriodo(resumen.getPeriodo());
        return reporte;
    }
}
