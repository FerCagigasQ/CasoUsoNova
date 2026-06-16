package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AmendmentDTO {
    private Long id;
    private LocalDate amendmentDate;
    private String description;
    private BigDecimal newAmount;
    private LocalDate newExpiryDate;

    public AmendmentDTO() {
    }

    public AmendmentDTO(Long id, LocalDate amendmentDate, String description, BigDecimal newAmount, LocalDate newExpiryDate) {
        this.id = id;
        this.amendmentDate = amendmentDate;
        this.description = description;
        this.newAmount = newAmount;
        this.newExpiryDate = newExpiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAmendmentDate() {
        return amendmentDate;
    }

    public void setAmendmentDate(LocalDate amendmentDate) {
        this.amendmentDate = amendmentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(BigDecimal newAmount) {
        this.newAmount = newAmount;
    }

    public LocalDate getNewExpiryDate() {
        return newExpiryDate;
    }

    public void setNewExpiryDate(LocalDate newExpiryDate) {
        this.newExpiryDate = newExpiryDate;
    }
}
