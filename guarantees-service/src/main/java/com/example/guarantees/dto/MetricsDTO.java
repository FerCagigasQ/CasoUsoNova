package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MetricsDTO {
    private Long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byType;
    private Map<String, Long> byCurrency;
    private List<MonthlyCountDTO> byMonth;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private Long activeCount;
    private Long expiringIn30Days;

    public MetricsDTO(Long total, Map<String, Long> byStatus, Map<String, Long> byType, List<MonthlyCountDTO> byMonth) {
        this(total, byStatus, byType, Map.of(), byMonth, BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L);
    }

    public MetricsDTO(Long total, Map<String, Long> byStatus, Map<String, Long> byType, Map<String, Long> byCurrency,
                      List<MonthlyCountDTO> byMonth, BigDecimal totalAmount, BigDecimal averageAmount,
                      Long activeCount, Long expiringIn30Days) {
        this.total = total;
        this.byStatus = byStatus;
        this.byType = byType;
        this.byCurrency = byCurrency;
        this.byMonth = byMonth;
        this.totalAmount = totalAmount;
        this.averageAmount = averageAmount;
        this.activeCount = activeCount;
        this.expiringIn30Days = expiringIn30Days;
    }

    public Long getTotal() { return total; }
    public Map<String, Long> getByStatus() { return byStatus; }
    public Map<String, Long> getByType() { return byType; }
    public Map<String, Long> getByCurrency() { return byCurrency; }
    public List<MonthlyCountDTO> getByMonth() { return byMonth; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getAverageAmount() { return averageAmount; }
    public Long getActiveCount() { return activeCount; }
    public Long getExpiringIn30Days() { return expiringIn30Days; }
}
