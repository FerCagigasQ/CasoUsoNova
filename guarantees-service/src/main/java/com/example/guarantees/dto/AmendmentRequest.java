package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AmendmentRequest {
    private BigDecimal newAmount;
    private LocalDate newExpiryDate;
    private String description;

    public AmendmentRequest() {}

    public BigDecimal getNewAmount() { return newAmount; }
    public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }

    public LocalDate getNewExpiryDate() { return newExpiryDate; }
    public void setNewExpiryDate(LocalDate newExpiryDate) { this.newExpiryDate = newExpiryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
