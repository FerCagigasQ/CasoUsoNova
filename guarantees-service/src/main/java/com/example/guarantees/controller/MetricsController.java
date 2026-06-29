package com.example.guarantees.controller;

import com.example.guarantees.dto.MetricsDTO;
import com.example.guarantees.dto.MonthlyCountDTO;
import com.example.guarantees.repository.GuaranteeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Tag(name = "Metrics", description = "KPI aggregations for the dashboard")
public class MetricsController {

    private final GuaranteeRepository repository;

    public MetricsController(GuaranteeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(
        summary = "Get platform KPI metrics",
        description = "Returns aggregated metrics for the dashboard: total guarantees, breakdown by status, by type, and monthly evolution."
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
                  "byMonth": [
                    { "month": "2024-01", "count": 1 },
                    { "month": "2024-02", "count": 1 },
                    { "month": "2024-03", "count": 1 },
                    { "month": "2024-04", "count": 1 },
                    { "month": "2024-05", "count": 1 },
                    { "month": "2024-11", "count": 1 }
                  ]
                }
                """)
        )
    )
    public ResponseEntity<MetricsDTO> getMetrics() {
        long total = repository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : repository.countByStatus()) {
            byStatus.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Long> byType = new LinkedHashMap<>();
        for (Object[] row : repository.countByType()) {
            byType.put(row[0].toString(), (Long) row[1]);
        }

        List<MonthlyCountDTO> byMonth = repository.countByMonth().stream()
            .map(row -> {
                int year = ((Number) row[0]).intValue();
                int month = ((Number) row[1]).intValue();
                String label = String.format("%d-%02d", year, month);
                return new MonthlyCountDTO(label, (Long) row[2]);
            })
            .toList();

        return ResponseEntity.ok(new MetricsDTO(total, byStatus, byType, byMonth));
    }
}
