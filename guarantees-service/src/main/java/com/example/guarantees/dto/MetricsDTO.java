package com.example.guarantees.dto;

import java.util.List;
import java.util.Map;

public class MetricsDTO {
    private Long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byType;
    private List<MonthlyCountDTO> byMonth;

    public MetricsDTO(Long total, Map<String, Long> byStatus, Map<String, Long> byType, List<MonthlyCountDTO> byMonth) {
        this.total = total;
        this.byStatus = byStatus;
        this.byType = byType;
        this.byMonth = byMonth;
    }

    public Long getTotal() { return total; }
    public Map<String, Long> getByStatus() { return byStatus; }
    public Map<String, Long> getByType() { return byType; }
    public List<MonthlyCountDTO> getByMonth() { return byMonth; }
}
