package com.example.guarantees.controller;

import com.example.guarantees.domain.IssuingBank;
import com.example.guarantees.repository.IssuingBankRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issuing-banks")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class IssuingBankController {

    private final IssuingBankRepository repository;

    public IssuingBankController(IssuingBankRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<IssuingBank>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
