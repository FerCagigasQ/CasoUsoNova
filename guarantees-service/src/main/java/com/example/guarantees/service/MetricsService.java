package com.example.guarantees.service;

import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.dto.MetricsDTO;
import com.example.guarantees.dto.MonthlyCountDTO;
import com.example.guarantees.dto.TopBeneficiaryDTO;
import com.example.guarantees.repository.GuaranteeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MetricsService {

    private static final List<GuaranteeStatus> ACTIVE_STATUSES = List.of(GuaranteeStatus.ISSUED, GuaranteeStatus.AMENDED);

    private final GuaranteeRepository repository;

    public MetricsService(GuaranteeRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "metrics", key = "{#status, #type, #currency, #issueDateFrom, #issueDateTo, #expiryDateFrom, #expiryDateTo}")
    public MetricsDTO getMetrics(GuaranteeStatus status, GuaranteeType type, String currency,
                                 LocalDate issueDateFrom, LocalDate issueDateTo,
                                 LocalDate expiryDateFrom, LocalDate expiryDateTo) {
        String normalizedCurrency = normalizeCurrency(currency);
        long total = repository.countFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo);
        BigDecimal totalAmount = safeAmount(repository.sumAmountFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo));
        BigDecimal averageAmount = total == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : totalAmount.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        Map<String, Long> byStatus = toCountMap(repository.countByStatusFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo));
        Map<String, Long> byType = toCountMap(repository.countByTypeFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo));
        Map<String, Long> byCurrency = toCountMap(repository.countByCurrencyFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo));
        Map<String, BigDecimal> totalAmountByCurrency = toAmountMap(repository.sumAmountByCurrencyFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo));
        List<MonthlyCountDTO> byMonth = repository.countByMonthFiltered(status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo).stream()
            .map(row -> {
                int year = ((Number) row[0]).intValue();
                int month = ((Number) row[1]).intValue();
                return new MonthlyCountDTO(String.format("%d-%02d", year, month), ((Number) row[2]).longValue());
            })
            .toList();

        long activeCount = repository.countFilteredByStatuses(ACTIVE_STATUSES, status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo);
        LocalDate today = LocalDate.now();
        long expiringIn30Days = repository.countExpiringBetweenFiltered(today, today.plusDays(30), status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo);
        List<TopBeneficiaryDTO> topBeneficiaries = repository.findTopBeneficiariesFiltered(
                status, type, normalizedCurrency, issueDateFrom, issueDateTo, expiryDateFrom, expiryDateTo, PageRequest.of(0, 5))
            .stream()
            .map(row -> new TopBeneficiaryDTO(
                ((Number) row[0]).longValue(),
                row[1].toString(),
                row[2].toString(),
                row[3].toString(),
                ((Number) row[4]).longValue(),
                safeAmount((BigDecimal) row[5])))
            .toList();

        return new MetricsDTO(total, byStatus, byType, byCurrency, byMonth, totalAmount, totalAmountByCurrency, averageAmount, activeCount, expiringIn30Days, topBeneficiaries);
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> values = new LinkedHashMap<>();
        for (Object[] row : rows) {
            values.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return values;
    }

    private Map<String, BigDecimal> toAmountMap(List<Object[]> rows) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        for (Object[] row : rows) {
            values.put(row[0].toString(), safeAmount((BigDecimal) row[1]));
        }
        return values;
    }

    private String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank() ? null : currency.trim().toUpperCase();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
