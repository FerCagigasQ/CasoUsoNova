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
    private Map<String, BigDecimal> totalAmountByCurrency;
    private BigDecimal averageAmount;
    private Long activeCount;
    private Long expiringIn30Days;
    private List<TopBeneficiaryDTO> topBeneficiaries;

    public MetricsDTO(Long total, Map<String, Long> byStatus, Map<String, Long> byType, List<MonthlyCountDTO> byMonth) {
        this(total, byStatus, byType, Map.of(), byMonth, BigDecimal.ZERO, Map.of(), BigDecimal.ZERO, 0L, 0L, List.of());
    }

    public MetricsDTO(Long total, Map<String, Long> byStatus, Map<String, Long> byType, Map<String, Long> byCurrency,
                      List<MonthlyCountDTO> byMonth, BigDecimal totalAmount, Map<String, BigDecimal> totalAmountByCurrency,
                      BigDecimal averageAmount, Long activeCount, Long expiringIn30Days,
                      List<TopBeneficiaryDTO> topBeneficiaries) {
        this.total = total;
        this.byStatus = byStatus;
        this.byType = byType;
        this.byCurrency = byCurrency;
        this.byMonth = byMonth;
        this.totalAmount = totalAmount;
        this.totalAmountByCurrency = totalAmountByCurrency;
        this.averageAmount = averageAmount;
        this.activeCount = activeCount;
        this.expiringIn30Days = expiringIn30Days;
        this.topBeneficiaries = topBeneficiaries;
    }

    public Long getTotal() { return total; }
    public Map<String, Long> getByStatus() { return byStatus; }
    public Map<String, Long> getByType() { return byType; }
    public Map<String, Long> getByCurrency() { return byCurrency; }
    public List<MonthlyCountDTO> getByMonth() { return byMonth; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Map<String, BigDecimal> getTotalAmountByCurrency() { return totalAmountByCurrency; }
    public BigDecimal getAverageAmount() { return averageAmount; }
    public Long getActiveCount() { return activeCount; }
    public Long getExpiringIn30Days() { return expiringIn30Days; }
    public List<TopBeneficiaryDTO> getTopBeneficiaries() { return topBeneficiaries; }
}
