package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GuaranteeDTO {
    private Long id;
    private String reference;
    private String type;
    private BigDecimal amount;
    private String currency;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private ApplicantDTO applicant;
    private BeneficiaryDTO beneficiary;
    private IssuingBankDTO issuingBank;
    private List<AmendmentDTO> amendments;
    private List<ClaimDTO> claims;

    public GuaranteeDTO() {
    }

    public GuaranteeDTO(Long id, String reference, String type, BigDecimal amount, String currency, LocalDate issueDate, LocalDate expiryDate, String status, ApplicantDTO applicant, BeneficiaryDTO beneficiary, IssuingBankDTO issuingBank, List<AmendmentDTO> amendments, List<ClaimDTO> claims) {
        this.id = id;
        this.reference = reference;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.applicant = applicant;
        this.beneficiary = beneficiary;
        this.issuingBank = issuingBank;
        this.amendments = amendments;
        this.claims = claims;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ApplicantDTO getApplicant() {
        return applicant;
    }

    public void setApplicant(ApplicantDTO applicant) {
        this.applicant = applicant;
    }

    public BeneficiaryDTO getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(BeneficiaryDTO beneficiary) {
        this.beneficiary = beneficiary;
    }

    public IssuingBankDTO getIssuingBank() {
        return issuingBank;
    }

    public void setIssuingBank(IssuingBankDTO issuingBank) {
        this.issuingBank = issuingBank;
    }

    public List<AmendmentDTO> getAmendments() {
        return amendments;
    }

    public void setAmendments(List<AmendmentDTO> amendments) {
        this.amendments = amendments;
    }

    public List<ClaimDTO> getClaims() {
        return claims;
    }

    public void setClaims(List<ClaimDTO> claims) {
        this.claims = claims;
    }
}
