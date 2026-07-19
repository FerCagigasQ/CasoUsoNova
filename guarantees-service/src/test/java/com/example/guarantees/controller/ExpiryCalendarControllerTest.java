package com.example.guarantees.controller;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.guarantees.domain.Applicant;
import com.example.guarantees.domain.Beneficiary;
import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.domain.IssuingBank;
import com.example.guarantees.repository.ApplicantRepository;
import com.example.guarantees.repository.BeneficiaryRepository;
import com.example.guarantees.repository.GuaranteeRepository;
import com.example.guarantees.repository.IssuingBankRepository;
import com.example.guarantees.service.GuaranteeEventService;
import com.example.guarantees.service.GuaranteeExpiryScheduler;
import com.example.guarantees.service.GuaranteeService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@AutoConfigureMockMvc
@Transactional
class ExpiryCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GuaranteeRepository guaranteeRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private IssuingBankRepository issuingBankRepository;

    @Autowired
    private GuaranteeService guaranteeService;

    @MockBean
    private Clock clock;

    @SpyBean
    private GuaranteeEventService guaranteeEventService;

    @MockBean
    private GuaranteeExpiryScheduler guaranteeExpiryScheduler;

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(clock.instant()).thenReturn(Instant.parse("2026-07-18T09:00:00Z"));
        guaranteeRepository.deleteAll();
    }

    @Test
    void getExpiryCalendar_returnsDailyAggregationsAndRiskLevels() throws Exception {
        saveGuarantee("CAL-001", new BigDecimal("40000.00"), "EUR", LocalDate.of(2026, 7, 20), GuaranteeStatus.ISSUED);
        saveGuarantee("CAL-002", new BigDecimal("250000.00"), "USD", LocalDate.of(2026, 7, 20), GuaranteeStatus.AMENDED);
        saveGuarantee("CAL-003", new BigDecimal("60000.00"), "EUR", LocalDate.of(2026, 7, 31), GuaranteeStatus.ISSUED);

        mockMvc.perform(get("/api/v1/guarantees/expiry-calendar").param("month", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-07"))
                .andExpect(jsonPath("$.days.length()").value(2))
                .andExpect(jsonPath("$.days[0].day").value(20))
                .andExpect(jsonPath("$.days[0].guarantees.length()").value(2))
                .andExpect(jsonPath("$.days[0].totalByAmount").value(290000.00))
                .andExpect(jsonPath("$.days[0].totalByCurrency.EUR").value(40000.00))
                .andExpect(jsonPath("$.days[0].totalByCurrency.USD").value(250000.00))
                .andExpect(jsonPath("$.days[0].aggregateRiskLevel").value("high"))
                .andExpect(jsonPath("$.days[0].guarantees[0].reference").value("CAL-001"))
                .andExpect(jsonPath("$.days[0].guarantees[0].daysUntilExpiry").value(2))
                .andExpect(jsonPath("$.days[0].guarantees[0].riskLevel").value("high"))
                .andExpect(jsonPath("$.days[0].guarantees[1].reference").value("CAL-002"))
                .andExpect(jsonPath("$.days[0].guarantees[1].riskLevel").value("high"))
                .andExpect(jsonPath("$.days[1].day").value(31))
                .andExpect(jsonPath("$.days[1].aggregateRiskLevel").value("medium"))
                .andExpect(jsonPath("$.riskCatalog.none").value("No vencimientos ese dia"))
                .andExpect(jsonPath("$.riskCatalog.critical").value("Vence hoy/ya vencio o importe > 1M"));
    }

    @Test
    void expireDueGuarantees_marksOnlyIssuedAndAmendedAsExpired() {
        Guarantee issued = saveGuarantee("EXP-001", new BigDecimal("150000.00"), "EUR", LocalDate.of(2026, 7, 17), GuaranteeStatus.ISSUED);
        Guarantee amended = saveGuarantee("EXP-002", new BigDecimal("175000.00"), "USD", LocalDate.of(2026, 7, 18), GuaranteeStatus.AMENDED);
        Guarantee claimed = saveGuarantee("EXP-003", new BigDecimal("200000.00"), "GBP", LocalDate.of(2026, 7, 17), GuaranteeStatus.CLAIMED);

        int expiredCount = guaranteeService.expireDueGuarantees();

        Guarantee updatedIssued = guaranteeRepository.findById(issued.getId()).orElseThrow();
        Guarantee updatedAmended = guaranteeRepository.findById(amended.getId()).orElseThrow();
        Guarantee updatedClaimed = guaranteeRepository.findById(claimed.getId()).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(2, expiredCount);
        org.junit.jupiter.api.Assertions.assertEquals(GuaranteeStatus.EXPIRED, updatedIssued.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(GuaranteeStatus.EXPIRED, updatedAmended.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(GuaranteeStatus.CLAIMED, updatedClaimed.getStatus());
        verify(guaranteeEventService, atLeastOnce()).publishEvent(
                org.mockito.ArgumentMatchers.eq("guarantee-events"),
                org.mockito.ArgumentMatchers.eq("expiration-auto"),
                org.mockito.ArgumentMatchers.anyMap());
    }

    private Guarantee saveGuarantee(String reference, BigDecimal amount, String currency, LocalDate expiryDate, GuaranteeStatus status) {
        Applicant applicant = applicantRepository.findAll().stream().findFirst().orElseThrow();
        Beneficiary beneficiary = beneficiaryRepository.findAll().stream().findFirst().orElseThrow();
        IssuingBank issuingBank = issuingBankRepository.findAll().stream().findFirst().orElseThrow();

        Guarantee guarantee = new Guarantee(
                reference,
                GuaranteeType.PERFORMANCE,
                amount,
                currency,
                expiryDate.minusDays(45),
                expiryDate,
                status,
                applicant,
                beneficiary,
                issuingBank);
        return guaranteeRepository.save(guarantee);
    }
}
