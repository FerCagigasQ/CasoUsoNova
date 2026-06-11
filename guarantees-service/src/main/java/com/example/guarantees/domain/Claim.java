package com.example.guarantees.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guarantee_id")
    private Guarantee guarantee;

    @Column(nullable = false)
    private LocalDate submissionDate;

    @Column(nullable = false)
    private BigDecimal claimedAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private String reason;

    private String reviewNotes;

    private LocalDate resolutionDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Claim() {
        this.createdAt = LocalDateTime.now();
    }

    public Claim(Guarantee guarantee, LocalDate submissionDate, BigDecimal claimedAmount,
                 ClaimStatus status, String reason) {
        this.guarantee = guarantee;
        this.submissionDate = submissionDate;
        this.claimedAmount = claimedAmount;
        this.status = status;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Guarantee getGuarantee() { return guarantee; }
    public void setGuarantee(Guarantee guarantee) { this.guarantee = guarantee; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
