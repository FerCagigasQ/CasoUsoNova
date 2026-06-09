package com.bbva.gdpd.pedidos.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MetricasNegocio {

    private final Counter transaccionesProcesadas;
    private final Counter transaccionesFallidas;
    private final Timer tiempoProcesamiento;

    public MetricasNegocio(MeterRegistry registry) {
        this.transaccionesProcesadas = Counter.builder("nova.transacciones.procesadas")
                .description("Total de transacciones procesadas correctamente")
                .tag("servicio", "gdpd-pedidos-api")
                .register(registry);

        this.transaccionesFallidas = Counter.builder("nova.transacciones.fallidas")
                .description("Total de transacciones con error")
                .tag("servicio", "gdpd-pedidos-api")
                .register(registry);

        this.tiempoProcesamiento = Timer.builder("nova.transacciones.tiempo")
                .description("Tiempo de procesamiento de transacciones")
                .publishPercentiles(0.5, 0.95, 0.99)
                .tag("servicio", "gdpd-pedidos-api")
                .register(registry);
    }

    public void registrarExito(Duration duracion) {
        transaccionesProcesadas.increment();
        tiempoProcesamiento.record(duracion);
    }

    public void registrarError() {
        transaccionesFallidas.increment();
    }
}
