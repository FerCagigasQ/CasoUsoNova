package com.example.guarantees.controller;

import com.example.guarantees.domain.Applicant;
import com.example.guarantees.repository.ApplicantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applicants")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class ApplicantController {

    private final ApplicantRepository repository;

    public ApplicantController(ApplicantRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Applicant>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
