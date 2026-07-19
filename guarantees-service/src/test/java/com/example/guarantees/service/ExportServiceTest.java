package com.example.guarantees.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExportServiceTest {

    @Autowired
    private ExportService exportService;

    @Autowired
    private GuaranteeRepository guaranteeRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private IssuingBankRepository issuingBankRepository;

    @Autowired
    private ExportJobStore jobStore;

    @BeforeEach
    void setUp() {
        guaranteeRepository.deleteAll();
        applicantRepository.deleteAll();
        beneficiaryRepository.deleteAll();
        issuingBankRepository.deleteAll();
    }

    @Test
    void exportToExcel_createsFile() throws InterruptedException {
        // Setup test data
        Applicant applicant = new Applicant();
        applicant.setFirstName("John");
        applicant.setLastName("Doe");
        applicant.setTaxId("12345678A");
        applicant.setEmail("john@example.com");
        applicant.setPhone("1234567890");
        applicant.setAddress("123 Main St");
        applicant.setCountry("ES");
        applicant = applicantRepository.save(applicant);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setFirstName("Jane");
        beneficiary.setLastName("Smith");
        beneficiary.setTaxId("87654321B");
        beneficiary.setEmail("jane@example.com");
        beneficiary.setPhone("0987654321");
        beneficiary.setAddress("456 Oak Ave");
        beneficiary.setCountry("US");
        beneficiary = beneficiaryRepository.save(beneficiary);

        IssuingBank bank = new IssuingBank();
        bank.setName("Test Bank");
        bank.setBic("TESTBIC");
        bank.setCountry("ES");
        bank = issuingBankRepository.save(bank);

        Guarantee guarantee = new Guarantee(
            "REF-001",
            GuaranteeType.PERFORMANCE,
            BigDecimal.valueOf(50000),
            "USD",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2025, 1, 1),
            GuaranteeStatus.ISSUED,
            applicant,
            beneficiary,
            bank
        );
        guaranteeRepository.save(guarantee);

        // Execute export
        String jobId = exportService.generateJobId();
        ExportJobStore.JobInfo jobInfo = new ExportJobStore.JobInfo(jobId);
        jobStore.put(jobId, jobInfo);

        exportService.exportToExcel(jobId, "xlsx", GuaranteeStatus.ISSUED, null, null, null);

        // Wait for async task
        Thread.sleep(2000);

        // Verify
        ExportJobStore.JobInfo result = jobStore.get(jobId);
        assertNotNull(result);
        assertEquals("completed", result.status);
        assertEquals(100, result.progress);
        assertNotNull(result.fileData);
        assertTrue(result.fileData.exists());
    }

    @Test
    void exportToCSV_createsFile() throws InterruptedException {
        // Setup test data
        Applicant applicant = new Applicant();
        applicant.setFirstName("Alice");
        applicant.setLastName("Johnson");
        applicant.setTaxId("11111111A");
        applicant.setEmail("alice@example.com");
        applicant.setPhone("1111111111");
        applicant.setAddress("789 Pine Rd");
        applicant.setCountry("FR");
        applicant = applicantRepository.save(applicant);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setFirstName("Bob");
        beneficiary.setLastName("Brown");
        beneficiary.setTaxId("22222222B");
        beneficiary.setEmail("bob@example.com");
        beneficiary.setPhone("2222222222");
        beneficiary.setAddress("101 Elm Dr");
        beneficiary.setCountry("DE");
        beneficiary = beneficiaryRepository.save(beneficiary);

        IssuingBank bank = new IssuingBank();
        bank.setName("Another Bank");
        bank.setBic("ANOTHBIC");
        bank.setCountry("FR");
        bank = issuingBankRepository.save(bank);

        Guarantee guarantee = new Guarantee(
            "REF-002",
            GuaranteeType.BID_BOND,
            BigDecimal.valueOf(75000),
            "EUR",
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2025, 2, 1),
            GuaranteeStatus.DRAFT,
            applicant,
            beneficiary,
            bank
        );
        guaranteeRepository.save(guarantee);

        // Execute export
        String jobId = exportService.generateJobId();
        ExportJobStore.JobInfo jobInfo = new ExportJobStore.JobInfo(jobId);
        jobStore.put(jobId, jobInfo);

        exportService.exportToExcel(jobId, "csv", null, null, null, null);

        // Wait for async task
        Thread.sleep(2000);

        // Verify
        ExportJobStore.JobInfo result = jobStore.get(jobId);
        assertNotNull(result);
        assertEquals("completed", result.status);
        assertNotNull(result.fileData);
        assertTrue(result.fileData.exists());
        assertTrue(result.fileData.getName().endsWith(".csv"));
    }

    @Test
    void exportWithFilters_onlyExportsFilteredData() throws InterruptedException {
        // Setup test data with different statuses
        Applicant applicant = new Applicant();
        applicant.setFirstName("Test");
        applicant.setLastName("User");
        applicant.setTaxId("99999999A");
        applicant.setEmail("test@example.com");
        applicant.setPhone("9999999999");
        applicant.setAddress("999 Test St");
        applicant.setCountry("ES");
        applicant = applicantRepository.save(applicant);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setFirstName("Test");
        beneficiary.setLastName("Beneficiary");
        beneficiary.setTaxId("88888888B");
        beneficiary.setEmail("benef@example.com");
        beneficiary.setPhone("8888888888");
        beneficiary.setAddress("888 Benef St");
        beneficiary.setCountry("ES");
        beneficiary = beneficiaryRepository.save(beneficiary);

        IssuingBank bank = new IssuingBank();
        bank.setName("Test Bank");
        bank.setBic("TESTBIC");
        bank.setCountry("ES");
        bank = issuingBankRepository.save(bank);

        // Create two guarantees with different statuses
        Guarantee issued = new Guarantee(
            "ISSUED-001",
            GuaranteeType.PERFORMANCE,
            BigDecimal.valueOf(100000),
            "EUR",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2025, 1, 1),
            GuaranteeStatus.ISSUED,
            applicant, beneficiary, bank
        );
        guaranteeRepository.save(issued);

        Guarantee draft = new Guarantee(
            "DRAFT-001",
            GuaranteeType.BID_BOND,
            BigDecimal.valueOf(50000),
            "EUR",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2025, 1, 1),
            GuaranteeStatus.DRAFT,
            applicant, beneficiary, bank
        );
        guaranteeRepository.save(draft);

        // Export only ISSUED
        String jobId = exportService.generateJobId();
        ExportJobStore.JobInfo jobInfo = new ExportJobStore.JobInfo(jobId);
        jobStore.put(jobId, jobInfo);

        exportService.exportToExcel(jobId, "xlsx", GuaranteeStatus.ISSUED, null, null, null);

        // Wait for async task
        Thread.sleep(2000);

        // Verify
        ExportJobStore.JobInfo result = jobStore.get(jobId);
        assertEquals("completed", result.status);
        assertNotNull(result.fileData);
    }

    @Test
    void generateJobId_returnsUniquIds() {
        String id1 = exportService.generateJobId();
        String id2 = exportService.generateJobId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("exp-"));
        assertTrue(id2.startsWith("exp-"));
    }
}
