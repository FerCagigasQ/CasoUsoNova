package com.example.guarantees.config;

import com.example.guarantees.domain.*;
import com.example.guarantees.repository.*;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer {
    private final IssuingBankRepository issuingBankRepository;
    private final ApplicantRepository applicantRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final GuaranteeRepository guaranteeRepository;

    public DataInitializer(IssuingBankRepository issuingBankRepository,
                          ApplicantRepository applicantRepository,
                          BeneficiaryRepository beneficiaryRepository,
                          GuaranteeRepository guaranteeRepository) {
        this.issuingBankRepository = issuingBankRepository;
        this.applicantRepository = applicantRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.guaranteeRepository = guaranteeRepository;
    }

    @PostConstruct
    public void initializeData() {
        // Create 3 issuing banks
        IssuingBank bank1 = new IssuingBank("BBVA", "BBVA Bank", "Spain");
        IssuingBank bank2 = new IssuingBank("SANTANDER", "Santander Bank", "Spain");
        IssuingBank bank3 = new IssuingBank("CAIXABANK", "CaixaBank", "Spain");

        issuingBankRepository.save(bank1);
        issuingBankRepository.save(bank2);
        issuingBankRepository.save(bank3);

        // Create 4 applicants
        Applicant applicant1 = new Applicant("John", "Doe", "12345678A", "john@example.com", "+34911234567");
        Applicant applicant2 = new Applicant("Jane", "Smith", "23456789B", "jane@example.com", "+34911234568");
        Applicant applicant3 = new Applicant("Carlos", "García", "34567890C", "carlos@example.com", "+34911234569");
        Applicant applicant4 = new Applicant("María", "López", "45678901D", "maria@example.com", "+34911234570");

        applicantRepository.save(applicant1);
        applicantRepository.save(applicant2);
        applicantRepository.save(applicant3);
        applicantRepository.save(applicant4);

        // Create 4 beneficiaries
        Beneficiary beneficiary1 = new Beneficiary("Alice", "Johnson", "56789012E", "alice@example.com", "+34911234571");
        Beneficiary beneficiary2 = new Beneficiary("Bob", "Williams", "67890123F", "bob@example.com", "+34911234572");
        Beneficiary beneficiary3 = new Beneficiary("Diego", "Martínez", "78901234G", "diego@example.com", "+34911234573");
        Beneficiary beneficiary4 = new Beneficiary("Isabel", "Rodríguez", "89012345H", "isabel@example.com", "+34911234574");

        beneficiaryRepository.save(beneficiary1);
        beneficiaryRepository.save(beneficiary2);
        beneficiaryRepository.save(beneficiary3);
        beneficiaryRepository.save(beneficiary4);

        // Create 6 guarantees in various states
        Guarantee g1 = new Guarantee("GUAR-2024-001", applicant1, beneficiary1, bank1,
            new BigDecimal("100000.00"), LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15), GuaranteeStatus.ACTIVE);
        g1.setDescription("Trade finance guarantee");

        Guarantee g2 = new Guarantee("GUAR-2024-002", applicant2, beneficiary2, bank2,
            new BigDecimal("250000.00"), LocalDate.of(2024, 2, 20), LocalDate.of(2026, 2, 20), GuaranteeStatus.ACTIVE);
        g2.setDescription("Performance bond");

        Guarantee g3 = new Guarantee("GUAR-2024-003", applicant3, beneficiary3, bank3,
            new BigDecimal("75000.00"), LocalDate.of(2023, 6, 10), LocalDate.of(2024, 6, 10), GuaranteeStatus.EXPIRED);
        g3.setDescription("Bid bond - Expired");

        Guarantee g4 = new Guarantee("GUAR-2024-004", applicant4, beneficiary4, bank1,
            new BigDecimal("500000.00"), LocalDate.of(2024, 3, 1), LocalDate.of(2027, 3, 1), GuaranteeStatus.AMENDED);
        g4.setDescription("Payment guarantee - Amended");

        Guarantee g5 = new Guarantee("GUAR-2024-005", applicant1, beneficiary2, bank2,
            new BigDecimal("150000.00"), LocalDate.of(2024, 4, 5), LocalDate.of(2025, 4, 5), GuaranteeStatus.CLAIMED);
        g5.setDescription("Retention guarantee - Claim submitted");

        Guarantee g6 = new Guarantee("GUAR-2024-006", applicant2, beneficiary3, bank3,
            new BigDecimal("320000.00"), LocalDate.of(2024, 5, 12), LocalDate.of(2025, 5, 12), GuaranteeStatus.ISSUED);
        g6.setDescription("Advanced payment guarantee");

        guaranteeRepository.save(g1);
        guaranteeRepository.save(g2);
        guaranteeRepository.save(g3);
        guaranteeRepository.save(g4);
        guaranteeRepository.save(g5);
        guaranteeRepository.save(g6);
    }
}
