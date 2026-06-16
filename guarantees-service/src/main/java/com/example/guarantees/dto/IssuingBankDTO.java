package com.example.guarantees.dto;

public class IssuingBankDTO {
    private Long id;
    private String name;
    private String bic;
    private String country;

    public IssuingBankDTO() {
    }

    public IssuingBankDTO(Long id, String name, String bic, String country) {
        this.id = id;
        this.name = name;
        this.bic = bic;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
