package com.example.guarantees.config;

import com.example.guarantees.domain.*;
import com.example.guarantees.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            IssuingBankRepository bankRepo,
            ApplicantRepository applicantRepo,
            BeneficiaryRepository beneficiaryRepo,
            GuaranteeRepository guaranteeRepo,
            AmendmentRepository amendmentRepo,
            ClaimRepository claimRepo) {

        return args -> {
            if (guaranteeRepo.count() > 0) return;

            // Issuing Banks
            IssuingBank bbva = bankRepo.save(new IssuingBank("BBVA Spain", "BBVAESMMXXX", "ES"));
            IssuingBank santander = bankRepo.save(new IssuingBank("Santander", "BSCHESMMXXX", "ES"));
            IssuingBank bnp = bankRepo.save(new IssuingBank("BNP Paribas", "BNPAFRPPXXX", "FR"));

            // Applicants
            Applicant applicant1 = applicantRepo.save(new Applicant(
                    "Carlos", "García López", "B-12345678",
                    "carlos.garcia@empresa.es", "+34 91 123 4567",
                    "Calle Gran Vía 28, Madrid", "ES"));
            Applicant applicant2 = applicantRepo.save(new Applicant(
                    "María", "Rodríguez Sánchez", "A-87654321",
                    "maria.rodriguez@corp.es", "+34 93 234 5678",
                    "Passeig de Gràcia 50, Barcelona", "ES"));
            Applicant applicant3 = applicantRepo.save(new Applicant(
                    "Jean", "Dupont", "FR-456789",
                    "j.dupont@societe.fr", "+33 1 23 45 67 89",
                    "15 Rue de la Paix, Paris", "FR"));
            Applicant applicant4 = applicantRepo.save(new Applicant(
                    "Hans", "Müller", "DE-789012",
                    "h.mueller@gmbh.de", "+49 30 987 6543",
                    "Unter den Linden 10, Berlin", "DE"));

            // Beneficiaries
            Beneficiary bene1 = beneficiaryRepo.save(new Beneficiary(
                    "Ahmed", "Al-Rashid", "AE-111222",
                    "ahmed.rashid@trading.ae", "+971 4 567 8901",
                    "Dubai International Financial Centre", "AE"));
            Beneficiary bene2 = beneficiaryRepo.save(new Beneficiary(
                    "Sofia", "Petrov", "RU-333444",
                    "s.petrov@import.ru", "+7 495 123 4567",
                    "Tverskaya 15, Moscow", "RU"));
            Beneficiary bene3 = beneficiaryRepo.save(new Beneficiary(
                    "Li", "Wei", "CN-555666",
                    "li.wei@enterprise.cn", "+86 10 8765 4321",
                    "Century Avenue 100, Shanghai", "CN"));
            Beneficiary bene4 = beneficiaryRepo.save(new Beneficiary(
                    "Fatima", "Benali", "MA-777888",
                    "f.benali@maroc.ma", "+212 522 345 678",
                    "Boulevard Mohammed V, Casablanca", "MA"));

            // Guarantees
            Guarantee g1 = new Guarantee(
                    "BG-2024-001", GuaranteeType.PERFORMANCE,
                    new BigDecimal("500000.00"), "EUR",
                    LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15),
                    GuaranteeStatus.ISSUED, applicant1, bene1, bbva);
            guaranteeRepo.save(g1);

            Guarantee g2 = new Guarantee(
                    "BG-2024-002", GuaranteeType.ADVANCE_PAYMENT,
                    new BigDecimal("1200000.00"), "USD",
                    LocalDate.of(2024, 2, 1), LocalDate.of(2024, 8, 1),
                    GuaranteeStatus.ISSUED, applicant2, bene2, santander);
            guaranteeRepo.save(g2);

            Guarantee g3 = new Guarantee(
                    "BG-2024-003", GuaranteeType.BID_BOND,
                    new BigDecimal("75000.00"), "EUR",
                    LocalDate.of(2024, 3, 10), LocalDate.of(2024, 6, 10),
                    GuaranteeStatus.EXPIRED, applicant3, bene3, bnp);
            guaranteeRepo.save(g3);

            Guarantee g4 = new Guarantee(
                    "BG-2024-004", GuaranteeType.WARRANTY,
                    new BigDecimal("320000.00"), "GBP",
                    LocalDate.of(2024, 4, 20), LocalDate.of(2026, 4, 20),
                    GuaranteeStatus.ISSUED, applicant4, bene4, bbva);

            // Amendment on g4
            g4.setStatus(GuaranteeStatus.AMENDED);
            Guarantee savedG4 = guaranteeRepo.save(g4);
            Amendment amd = new Amendment();
            amd.setGuarantee(savedG4);
            amd.setAmendmentDate(LocalDate.of(2024, 7, 1));
            amd.setDescription("Extension of guarantee period and amount increase due to project scope change.");
            amd.setNewAmount(new BigDecimal("380000.00"));
            amd.setNewExpiryDate(LocalDate.of(2027, 4, 20));
            amendmentRepo.save(amd);
            savedG4.getAmendments().add(amd);
            savedG4.setAmount(new BigDecimal("380000.00"));
            savedG4.setExpiryDate(LocalDate.of(2027, 4, 20));
            guaranteeRepo.save(savedG4);

            Guarantee g5 = new Guarantee(
                    "BG-2024-005", GuaranteeType.PERFORMANCE,
                    new BigDecimal("900000.00"), "USD",
                    LocalDate.of(2024, 5, 5), LocalDate.of(2025, 5, 5),
                    GuaranteeStatus.ISSUED, applicant1, bene3, santander);
            // Claim on g5
            g5.setStatus(GuaranteeStatus.CLAIMED);
            Guarantee savedG5 = guaranteeRepo.save(g5);
            Claim clm = new Claim();
            clm.setGuarantee(savedG5);
            clm.setClaimDate(LocalDate.of(2024, 9, 15));
            clm.setClaimedAmount(new BigDecimal("450000.00"));
            clm.setStatus(ClaimStatus.UNDER_REVIEW);
            clm.setReason("Contractor failed to meet project milestones as per contract clause 12.3.");
            claimRepo.save(clm);
            savedG5.getClaims().add(clm);
            guaranteeRepo.save(savedG5);

            Guarantee g6 = new Guarantee(
                    "BG-2024-006", GuaranteeType.ADVANCE_PAYMENT,
                    new BigDecimal("250000.00"), "EUR",
                    LocalDate.of(2024, 11, 1), LocalDate.of(2025, 11, 1),
                    GuaranteeStatus.DRAFT, applicant2, bene1, bnp);
            guaranteeRepo.save(g6);

            // Vencimientos inminentes relativos a la fecha actual: alimentan el heatmap
            // de /calendar y permiten ver la expiración automática en vivo (demo 9)
            LocalDate today = LocalDate.now();

            Guarantee g7 = new Guarantee(
                    "BG-2026-007", GuaranteeType.PERFORMANCE,
                    new BigDecimal("150000.00"), "EUR",
                    today.minusMonths(6), today,
                    GuaranteeStatus.ISSUED, applicant1, bene2, bbva);
            guaranteeRepo.save(g7);

            Guarantee g8 = new Guarantee(
                    "BG-2026-008", GuaranteeType.BID_BOND,
                    new BigDecimal("45000.00"), "EUR",
                    today.minusMonths(3), today.plusDays(3),
                    GuaranteeStatus.ISSUED, applicant3, bene3, bnp);
            guaranteeRepo.save(g8);

            Guarantee g9 = new Guarantee(
                    "BG-2026-009", GuaranteeType.WARRANTY,
                    new BigDecimal("300000.00"), "USD",
                    today.minusMonths(2), today.plusDays(15),
                    GuaranteeStatus.ISSUED, applicant4, bene4, santander);
            guaranteeRepo.save(g9);

            Guarantee g10 = new Guarantee(
                    "BG-2026-010", GuaranteeType.ADVANCE_PAYMENT,
                    new BigDecimal("1500000.00"), "EUR",
                    today.minusMonths(1), today.plusDays(25),
                    GuaranteeStatus.ISSUED, applicant2, bene1, bbva);
            guaranteeRepo.save(g10);
        };
    }
}
