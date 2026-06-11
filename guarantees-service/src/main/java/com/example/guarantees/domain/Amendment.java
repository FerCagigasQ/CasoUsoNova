package com.example.guarantees.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "amendments")
public class Amendment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guarantee_id")
    private Guarantee guarantee;

    @Column(nullable = false)
    private LocalDate amendmentDate;

    private String description;

    private BigDecimal newAmount;

    private LocalDate newExpiryDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Amendment() {
        this.createdAt = LocalDateTime.now();
    }

    public Amendment(Guarantee guarantee, LocalDate amendmentDate, String description) {
        this.guarantee = guarantee;
        this.amendmentDate = amendmentDate;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Guarantee getGuarantee() { return guarantee; }
    public void setGuarantee(Guarantee guarantee) { this.guarantee = guarantee; }

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
