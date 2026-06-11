package com.example.guarantees.controller;

import com.example.guarantees.dto.ApplicantDTO;
import com.example.guarantees.service.ApplicantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applicants")
@Tag(name = "Applicants", description = "Manage applicants")
public class ApplicantController {
    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @GetMapping
    @Operation(summary = "Get all applicants")
    public ResponseEntity<List<ApplicantDTO>> getAllApplicants() {
        List<ApplicantDTO> applicants = applicantService.getAllApplicants();
        return ResponseEntity.ok(applicants);
    }
}
