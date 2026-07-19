package com.example.guarantees.service;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.dto.ExpiryCalendarBeneficiaryDTO;
import com.example.guarantees.dto.ExpiryCalendarDTO;
import com.example.guarantees.dto.ExpiryCalendarDayDTO;
import com.example.guarantees.dto.ExpiryCalendarGuaranteeDTO;
import com.example.guarantees.repository.GuaranteeRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExpiryCalendarService {

    private static final BigDecimal MEDIUM_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("200000");
    private static final BigDecimal CRITICAL_AMOUNT_THRESHOLD = new BigDecimal("1000000");
    private static final Set<GuaranteeStatus> CALENDAR_EXCLUDED_STATUSES = EnumSet.of(GuaranteeStatus.CANCELLED);

    private final GuaranteeRepository guaranteeRepository;
    private final Clock clock;

    public ExpiryCalendarService(GuaranteeRepository guaranteeRepository, Clock clock) {
        this.guaranteeRepository = guaranteeRepository;
        this.clock = clock;
    }

    public ExpiryCalendarDTO getMonthlyCalendar(YearMonth month) {
        LocalDate today = LocalDate.now(clock);
        List<Guarantee> guarantees = guaranteeRepository
                .findByExpiryDateBetweenAndStatusNotInOrderByExpiryDateAscReferenceAsc(
                        month.atDay(1),
                        month.atEndOfMonth(),
                        new ArrayList<>(CALENDAR_EXCLUDED_STATUSES));

        Map<LocalDate, List<Guarantee>> byDay = guarantees.stream()
                .collect(Collectors.groupingBy(Guarantee::getExpiryDate, LinkedHashMap::new, Collectors.toList()));

        List<ExpiryCalendarDayDTO> days = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toDayDto(today, entry.getKey(), entry.getValue()))
                .toList();

        return new ExpiryCalendarDTO(month.toString(), days, riskCatalog());
    }

    private ExpiryCalendarDayDTO toDayDto(LocalDate today, LocalDate day, List<Guarantee> guarantees) {
        List<ExpiryCalendarGuaranteeDTO> guaranteeDtos = guarantees.stream()
                .sorted(Comparator.comparing(Guarantee::getReference))
                .map(guarantee -> toGuaranteeDto(today, guarantee))
                .toList();

        BigDecimal totalAmount = guarantees.stream()
                .map(Guarantee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> totalByCurrency = guarantees.stream()
                .collect(Collectors.groupingBy(
                        guarantee -> guarantee.getCurrency().toUpperCase(Locale.ROOT),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Guarantee::getAmount, BigDecimal::add)));

        String aggregateRiskLevel = guaranteeDtos.stream()
                .map(ExpiryCalendarGuaranteeDTO::riskLevel)
                .max(Comparator.comparingInt(this::riskPriority))
                .orElse("none");

        return new ExpiryCalendarDayDTO(day.getDayOfMonth(), guaranteeDtos, totalAmount, totalByCurrency, aggregateRiskLevel);
    }

    private ExpiryCalendarGuaranteeDTO toGuaranteeDto(LocalDate today, Guarantee guarantee) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, guarantee.getExpiryDate());
        return new ExpiryCalendarGuaranteeDTO(
                guarantee.getId(),
                guarantee.getReference(),
                new ExpiryCalendarBeneficiaryDTO(
                        guarantee.getBeneficiary().getFirstName(),
                        guarantee.getBeneficiary().getLastName()),
                guarantee.getAmount(),
                guarantee.getCurrency().toUpperCase(Locale.ROOT),
                guarantee.getExpiryDate(),
                daysUntilExpiry,
                calculateRiskLevel(daysUntilExpiry, guarantee.getAmount()));
    }

    String calculateRiskLevel(long daysUntilExpiry, BigDecimal amount) {
        if (daysUntilExpiry <= 0 || amount.compareTo(CRITICAL_AMOUNT_THRESHOLD) > 0) {
            return "critical";
        }
        if (daysUntilExpiry <= 7 || amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            return "high";
        }
        if (daysUntilExpiry <= 30 || amount.compareTo(MEDIUM_AMOUNT_THRESHOLD) >= 0) {
            return "medium";
        }
        return "low";
    }

    private Map<String, String> riskCatalog() {
        Map<String, String> catalog = new LinkedHashMap<>();
        catalog.put("none", "No vencimientos ese dia");
        catalog.put("low", ">30 dias y < 50k");
        catalog.put("medium", "8-30 dias o importe entre 50k y 200k");
        catalog.put("high", "1-7 dias o importe entre 200k y 1M");
        catalog.put("critical", "Vence hoy/ya vencio o importe > 1M");
        return catalog;
    }

    private int riskPriority(String riskLevel) {
        return switch (riskLevel) {
            case "critical" -> 4;
            case "high" -> 3;
            case "medium" -> 2;
            case "low" -> 1;
            default -> 0;
        };
    }
}
