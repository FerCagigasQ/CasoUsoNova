package com.example.guarantees.controller;

import com.example.guarantees.dto.IssuingBankDTO;
import com.example.guarantees.service.IssuingBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/issuing-banks")
@Tag(name = "Issuing Banks", description = "Manage issuing banks")
public class IssuingBankController {
    private final IssuingBankService issuingBankService;

    public IssuingBankController(IssuingBankService issuingBankService) {
        this.issuingBankService = issuingBankService;
    }

    @GetMapping
    @Operation(summary = "Get all issuing banks")
    public ResponseEntity<List<IssuingBankDTO>> getAllIssuingBanks() {
        List<IssuingBankDTO> banks = issuingBankService.getAllIssuingBanks();
        return ResponseEntity.ok(banks);
    }
}
