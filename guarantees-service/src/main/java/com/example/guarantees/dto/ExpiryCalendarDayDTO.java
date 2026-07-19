package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ExpiryCalendarDayDTO(
        int day,
        List<ExpiryCalendarGuaranteeDTO> guarantees,
        BigDecimal totalByAmount,
        Map<String, BigDecimal> totalByCurrency,
        String aggregateRiskLevel) {
}
