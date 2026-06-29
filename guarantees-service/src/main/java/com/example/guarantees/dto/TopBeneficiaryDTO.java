package com.example.guarantees.dto;

import java.math.BigDecimal;

public class TopBeneficiaryDTO {
    private Long beneficiaryId;
    private String firstName;
    private String lastName;
    private String taxId;
    private Long guaranteeCount;
    private BigDecimal totalAmount;

    public TopBeneficiaryDTO(Long beneficiaryId, String firstName, String lastName, String taxId,
                             Long guaranteeCount, BigDecimal totalAmount) {
        this.beneficiaryId = beneficiaryId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxId = taxId;
        this.guaranteeCount = guaranteeCount;
        this.totalAmount = totalAmount;
    }

    public Long getBeneficiaryId() { return beneficiaryId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getTaxId() { return taxId; }
    public Long getGuaranteeCount() { return guaranteeCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
