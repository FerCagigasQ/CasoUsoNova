package com.example.guarantees.controller;

import com.example.guarantees.domain.Beneficiary;
import com.example.guarantees.repository.BeneficiaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class BeneficiaryController {

    private final BeneficiaryRepository repository;

    public BeneficiaryController(BeneficiaryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Beneficiary>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
