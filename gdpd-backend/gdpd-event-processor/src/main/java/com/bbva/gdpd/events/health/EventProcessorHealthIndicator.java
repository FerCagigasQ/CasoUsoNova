package com.bbva.gdpd.events.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class EventProcessorHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("service", "gdpd-event-processor")
                    .withDetail("status", "Operational")
                    .withDetail("type", "Daemon Consumer")
                    .withDetail("topic", "pedidos.eventos")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("service", "gdpd-event-processor")
                    .build();
        }
    }
}
