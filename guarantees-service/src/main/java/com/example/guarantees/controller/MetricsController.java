package com.example.guarantees.controller;

import com.example.guarantees.dto.MetricsDTO;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.service.MetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/metrics")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Tag(name = "Metrics", description = "KPI aggregations for the dashboard")
public class MetricsController {

    private final MetricsService metricsService;
    private final MeterRegistry meterRegistry;

    public MetricsController(MetricsService metricsService, MeterRegistry meterRegistry) {
        this.metricsService = metricsService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    @Operation(
        summary = "Get platform KPI metrics",
        description = "Returns filtered dashboard metrics: totals, amount KPIs, breakdowns by status/type/currency, and monthly evolution."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Aggregated metrics",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                  "total": 6,
                  "byStatus": { "ISSUED": 2, "AMENDED": 1, "CLAIMED": 1, "EXPIRED": 1, "DRAFT": 1 },
                  "byType": { "PERFORMANCE": 2, "ADVANCE_PAYMENT": 2, "BID_BOND": 1, "WARRANTY": 1 },
                  "byCurrency": { "EUR": 3, "GBP": 1, "USD": 2 },
                  "byMonth": [
                    { "month": "2024-01", "count": 1 },
                    { "month": "2024-02", "count": 1 },
                    { "month": "2024-03", "count": 1 },
                    { "month": "2024-04", "count": 1 },
                    { "month": "2024-05", "count": 1 },
                    { "month": "2024-11", "count": 1 }
                  ],
                  "totalAmount": 3305000.00,
                  "totalAmountByCurrency": { "EUR": 825000.00, "GBP": 380000.00, "USD": 2100000.00 },
                  "averageAmount": 550833.33,
                  "activeCount": 3,
                  "expiringIn30Days": 0,
                  "topBeneficiaries": [
                    { "beneficiaryId": 3, "firstName": "Li", "lastName": "Wei", "taxId": "CN-555666", "guaranteeCount": 2, "totalAmount": 975000.00 }
                  ]
                }
                """)
        )
    )
    public ResponseEntity<MetricsDTO> getMetrics(
        @Parameter(description = "Filter by guarantee status") @RequestParam(required = false) GuaranteeStatus status,
        @Parameter(description = "Filter by guarantee type") @RequestParam(required = false) GuaranteeType type,
        @Parameter(description = "Filter by ISO currency code") @RequestParam(required = false) String currency,
        @Parameter(description = "Alias for issueDateFrom. Filter by issue date from, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @Parameter(description = "Alias for issueDateTo. Filter by issue date to, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Parameter(description = "Filter by issue date from, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateFrom,
        @Parameter(description = "Filter by issue date to, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateTo,
        @Parameter(description = "Filter by expiry date from, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateFrom,
        @Parameter(description = "Filter by expiry date to, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateTo) {

        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            LocalDate effectiveIssueDateFrom = issueDateFrom != null ? issueDateFrom : from;
            LocalDate effectiveIssueDateTo = issueDateTo != null ? issueDateTo : to;
            MetricsDTO metrics = metricsService.getMetrics(status, type, currency, effectiveIssueDateFrom, effectiveIssueDateTo, expiryDateFrom, expiryDateTo);
            return ResponseEntity.ok(metrics);
        } catch (RuntimeException ex) {
            outcome = "error";
            throw ex;
        } finally {
            metricsRequestCounter(outcome).increment();
            sample.stop(metricsResponseTimer(outcome));
        }
    }

    private Counter metricsRequestCounter(String outcome) {
        return Counter.builder("nova.metrics.requests")
            .description("Total requests to the dashboard metrics API")
            .tag("endpoint", "/api/v1/metrics")
            .tag("outcome", outcome)
            .register(meterRegistry);
    }

    private Timer metricsResponseTimer(String outcome) {
        return Timer.builder("nova.metrics.response.time")
            .description("Response time for the dashboard metrics API")
            .tag("endpoint", "/api/v1/metrics")
            .tag("outcome", outcome)
            .register(meterRegistry);
    }
}
