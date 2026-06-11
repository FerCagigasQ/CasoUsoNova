package com.example.guarantees.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "guarantees")
public class Guarantee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beneficiary_id")
    private Beneficiary beneficiary;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issuing_bank_id")
    private IssuingBank issuingBank;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GuaranteeStatus status;

    private String description;

    @OneToMany(mappedBy = "guarantee", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Amendment> amendments = new HashSet<>();

    @OneToMany(mappedBy = "guarantee", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Claim> claims = new HashSet<>();

    public Guarantee() {}

    public Guarantee(String referenceNumber, Applicant applicant, Beneficiary beneficiary,
                     IssuingBank issuingBank, BigDecimal amount, LocalDate issueDate,
                     LocalDate expiryDate, GuaranteeStatus status) {
        this.referenceNumber = referenceNumber;
        this.applicant = applicant;
        this.beneficiary = beneficiary;
        this.issuingBank = issuingBank;
        this.amount = amount;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }

    public Beneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(Beneficiary beneficiary) { this.beneficiary = beneficiary; }

    public IssuingBank getIssuingBank() { return issuingBank; }
    public void setIssuingBank(IssuingBank issuingBank) { this.issuingBank = issuingBank; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public GuaranteeStatus getStatus() { return status; }
    public void setStatus(GuaranteeStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Amendment> getAmendments() { return amendments; }
    public void setAmendments(Set<Amendment> amendments) { this.amendments = amendments; }

    public Set<Claim> getClaims() { return claims; }
    public void setClaims(Set<Claim> claims) { this.claims = claims; }
}
