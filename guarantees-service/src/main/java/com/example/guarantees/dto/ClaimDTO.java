package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimDTO {
    private Long id;
    private LocalDate claimDate;
    private BigDecimal claimedAmount;
    private String status;
    private String reason;

    public ClaimDTO() {
    }

    public ClaimDTO(Long id, LocalDate claimDate, BigDecimal claimedAmount, String status, String reason) {
        this.id = id;
        this.claimDate = claimDate;
        this.claimedAmount = claimedAmount;
        this.status = status;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }

    public void setClaimedAmount(BigDecimal claimedAmount) {
        this.claimedAmount = claimedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
