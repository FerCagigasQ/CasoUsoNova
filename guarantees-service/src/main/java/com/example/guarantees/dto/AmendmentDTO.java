package com.example.guarantees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AmendmentDTO {
    private Long id;
    private Long guaranteeId;
    private LocalDate amendmentDate;
    private String description;
    private BigDecimal newAmount;
    private LocalDate newExpiryDate;
    private LocalDateTime createdAt;

    public AmendmentDTO() {}

    public AmendmentDTO(Long id, Long guaranteeId, LocalDate amendmentDate, String description,
                        BigDecimal newAmount, LocalDate newExpiryDate, LocalDateTime createdAt) {
        this.id = id;
        this.guaranteeId = guaranteeId;
        this.amendmentDate = amendmentDate;
        this.description = description;
        this.newAmount = newAmount;
        this.newExpiryDate = newExpiryDate;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuaranteeId() { return guaranteeId; }
    public void setGuaranteeId(Long guaranteeId) { this.guaranteeId = guaranteeId; }

    public LocalDate getAmendmentDate() { return amendmentDate; }
    public void setAmendmentDate(LocalDate amendmentDate) { this.amendmentDate = amendmentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getNewAmount() { return newAmount; }
    public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }

    public LocalDate getNewExpiryDate() { return newExpiryDate; }
    public void setNewExpiryDate(LocalDate newExpiryDate) { this.newExpiryDate = newExpiryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
