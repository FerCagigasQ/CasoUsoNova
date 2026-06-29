package com.example.guarantees.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
public class PrometheusDemoController {

    private final MeterRegistry meterRegistry;

    public PrometheusDemoController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping(path = "/prometheus", produces = MediaType.TEXT_PLAIN_VALUE)
    public String prometheus() {
        double requests = meterRegistry.find("nova.metrics.requests").counters().stream()
                .mapToDouble(counter -> counter.count())
                .sum();
        double responseTimeCount = meterRegistry.find("nova.metrics.response.time").timers().stream()
                .mapToDouble(timer -> timer.count())
                .sum();

        return """
                # HELP nova_metrics_requests_total Total requests to the dashboard metrics API
                # TYPE nova_metrics_requests_total counter
                nova_metrics_requests_total %s
                # HELP nova_metrics_response_time_seconds Response time for the dashboard metrics API
                # TYPE nova_metrics_response_time_seconds summary
                nova_metrics_response_time_seconds_count %s
                """.formatted(requests, responseTimeCount);
    }
}
