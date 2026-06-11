package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClaimDTO {
    private Long id;
    private Long guaranteeId;
    private LocalDate submissionDate;
    private BigDecimal claimedAmount;
    private String status;
    private String reason;
    private String reviewNotes;
    private LocalDate resolutionDate;
    private LocalDateTime createdAt;

    public ClaimDTO() {}

    public ClaimDTO(Long id, Long guaranteeId, LocalDate submissionDate, BigDecimal claimedAmount,
                    String status, String reason, String reviewNotes, LocalDate resolutionDate,
                    LocalDateTime createdAt) {
        this.id = id;
        this.guaranteeId = guaranteeId;
        this.submissionDate = submissionDate;
        this.claimedAmount = claimedAmount;
        this.status = status;
        this.reason = reason;
        this.reviewNotes = reviewNotes;
        this.resolutionDate = resolutionDate;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuaranteeId() { return guaranteeId; }
    public void setGuaranteeId(Long guaranteeId) { this.guaranteeId = guaranteeId; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
