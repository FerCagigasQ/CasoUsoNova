package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGuaranteeRequest {
    private String reference;
    private String type;
    private BigDecimal amount;
    private String currency;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Long applicantId;
    private Long beneficiaryId;
    private Long issuingBankId;

    public CreateGuaranteeRequest() {
    }

    public CreateGuaranteeRequest(String reference, String type, BigDecimal amount, String currency, LocalDate issueDate, LocalDate expiryDate, Long applicantId, Long beneficiaryId, Long issuingBankId) {
        this.reference = reference;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.applicantId = applicantId;
        this.beneficiaryId = beneficiaryId;
        this.issuingBankId = issuingBankId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public Long getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(Long beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public Long getIssuingBankId() {
        return issuingBankId;
    }

    public void setIssuingBankId(Long issuingBankId) {
        this.issuingBankId = issuingBankId;
    }
}
