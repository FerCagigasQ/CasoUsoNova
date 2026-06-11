package com.example.guarantees.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true)
    private String taxId;

    private String email;

    private String phone;

    @OneToMany(mappedBy = "beneficiary")
    private Set<Guarantee> guarantees = new HashSet<>();

    public Beneficiary() {}

    public Beneficiary(String firstName, String lastName, String taxId, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Set<Guarantee> getGuarantees() { return guarantees; }
    public void setGuarantees(Set<Guarantee> guarantees) { this.guarantees = guarantees; }
}
