package com.example.guarantees.dto;

public class IssuingBankDTO {
    private Long id;
    private String code;
    private String name;
    private String country;

    public IssuingBankDTO() {}

    public IssuingBankDTO(Long id, String code, String name, String country) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.country = country;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
