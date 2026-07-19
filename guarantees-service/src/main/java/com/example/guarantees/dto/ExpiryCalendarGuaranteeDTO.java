package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpiryCalendarGuaranteeDTO(
        Long id,
        String reference,
        ExpiryCalendarBeneficiaryDTO beneficiary,
        BigDecimal amount,
        String currency,
        LocalDate expiryDate,
        long daysUntilExpiry,
        String riskLevel) {
}
