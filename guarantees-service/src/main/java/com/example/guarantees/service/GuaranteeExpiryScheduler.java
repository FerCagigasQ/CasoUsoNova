package com.example.guarantees.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GuaranteeExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(GuaranteeExpiryScheduler.class);

    private final GuaranteeService guaranteeService;
    private final Counter expiredCounter;
    private final Timer schedulerTimer;
    private final MeterRegistry meterRegistry;

    public GuaranteeExpiryScheduler(GuaranteeService guaranteeService, MeterRegistry meterRegistry) {
        this.guaranteeService = guaranteeService;
        this.meterRegistry = meterRegistry;
        this.expiredCounter = Counter.builder("guarantees_expired_auto_total")
                .description("Total number of guarantees auto-expired by scheduler")
                .register(meterRegistry);
        this.schedulerTimer = Timer.builder("guarantees_expiry_scheduler_duration_seconds")
                .description("Execution duration of guarantee expiry scheduler")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @Scheduled(fixedRateString = "${guarantees.expiry.scheduler.fixed-rate-ms:30000}")
    public void expireDueGuarantees() {
        long startMs = System.currentTimeMillis();

        int expiredCount = guaranteeService.expireDueGuarantees();

        long durationMs = System.currentTimeMillis() - startMs;
        expiredCounter.increment(expiredCount);
        schedulerTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        long expiringCount = guaranteeService.countExpiringWithinDays(7);
        meterRegistry.gauge("guarantees_expiring_soon_count", expiringCount);

        log.info("Scheduler: expired {} guarantees in {} ms", expiredCount, durationMs);
    }
}
