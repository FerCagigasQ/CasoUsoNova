package com.example.guarantees.dto;

import java.math.BigDecimal;

public class ClaimRequest {
    private BigDecimal claimedAmount;
    private String reason;

    public ClaimRequest() {}

    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
