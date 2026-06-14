package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GuaranteeDTO {
    private Long id;
    private String referenceNumber;
    private ApplicantDTO applicant;
    private BeneficiaryDTO beneficiary;
    private IssuingBankDTO issuingBank;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private String description;

    public GuaranteeDTO() {}

    public GuaranteeDTO(Long id, String referenceNumber, ApplicantDTO applicant,
                        BeneficiaryDTO beneficiary, IssuingBankDTO issuingBank,
                        BigDecimal amount, LocalDate issueDate, LocalDate expiryDate,
                        String status, String description) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.applicant = applicant;
        this.beneficiary = beneficiary;
        this.issuingBank = issuingBank;
        this.amount = amount;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public ApplicantDTO getApplicant() { return applicant; }
    public void setApplicant(ApplicantDTO applicant) { this.applicant = applicant; }

    public BeneficiaryDTO getBeneficiary() { return beneficiary; }
    public void setBeneficiary(BeneficiaryDTO beneficiary) { this.beneficiary = beneficiary; }

    public IssuingBankDTO getIssuingBank() { return issuingBank; }
    public void setIssuingBank(IssuingBankDTO issuingBank) { this.issuingBank = issuingBank; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
