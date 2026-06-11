package com.example.guarantees.controller;

import com.example.guarantees.dto.BeneficiaryDTO;
import com.example.guarantees.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiaries", description = "Manage beneficiaries")
public class BeneficiaryController {
    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    @Operation(summary = "Get all beneficiaries")
    public ResponseEntity<List<BeneficiaryDTO>> getAllBeneficiaries() {
        List<BeneficiaryDTO> beneficiaries = beneficiaryService.getAllBeneficiaries();
        return ResponseEntity.ok(beneficiaries);
    }
}
