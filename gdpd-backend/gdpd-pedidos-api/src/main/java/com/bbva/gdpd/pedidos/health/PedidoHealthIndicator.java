package com.bbva.gdpd.pedidos.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PedidoHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("service", "gdpd-pedidos-api")
                    .withDetail("status", "Operational")
                    .withDetail("endpoints", "/actuator/health, /actuator/metrics, /actuator/prometheus")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("service", "gdpd-pedidos-api")
                    .build();
        }
    }
}
