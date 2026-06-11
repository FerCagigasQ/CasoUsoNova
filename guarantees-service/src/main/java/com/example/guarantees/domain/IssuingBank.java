package com.example.guarantees.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "issuing_banks")
public class IssuingBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String country;

    @OneToMany(mappedBy = "issuingBank", cascade = CascadeType.ALL)
    private Set<Guarantee> guarantees = new HashSet<>();

    public IssuingBank() {}

    public IssuingBank(String code, String name, String country) {
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

    public Set<Guarantee> getGuarantees() { return guarantees; }
    public void setGuarantees(Set<Guarantee> guarantees) { this.guarantees = guarantees; }
}
