package com.example.guarantees.controller;

import com.example.guarantees.dto.MetricsDTO;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.service.MetricsService;
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

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
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
                  "averageAmount": 550833.33,
                  "activeCount": 3,
                  "expiringIn30Days": 0
                }
                """)
        )
    )
    public ResponseEntity<MetricsDTO> getMetrics(
        @Parameter(description = "Filter by guarantee status") @RequestParam(required = false) GuaranteeStatus status,
        @Parameter(description = "Filter by guarantee type") @RequestParam(required = false) GuaranteeType type,
        @Parameter(description = "Filter by ISO currency code") @RequestParam(required = false) String currency,
        @Parameter(description = "Filter by issue date from, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateFrom,
        @Parameter(description = "Filter by issue date to, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateTo,
        @Parameter(description = "Filter by expiry date from, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateFrom,
        @Parameter(description = "Filter by expiry date to, inclusive, ISO-8601 yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateTo) {

        MetricsDTO metrics = metricsService.getMetrics(status, type, currency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo);
        return ResponseEntity.ok(metrics);
    }
}
