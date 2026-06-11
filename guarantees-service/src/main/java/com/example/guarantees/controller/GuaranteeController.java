package com.example.guarantees.controller;

import com.example.guarantees.dto.*;
import com.example.guarantees.service.GuaranteeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/guarantees")
@Tag(name = "Guarantees", description = "Manage guarantees")
public class GuaranteeController {
    private final GuaranteeService guaranteeService;

    public GuaranteeController(GuaranteeService guaranteeService) {
        this.guaranteeService = guaranteeService;
    }

    @PostMapping
    @Operation(summary = "Create a new guarantee")
    public ResponseEntity<GuaranteeDTO> createGuarantee(@RequestBody GuaranteeDTO dto) {
        GuaranteeDTO created = guaranteeService.createGuarantee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all guarantees")
    public ResponseEntity<List<GuaranteeDTO>> getAllGuarantees() {
        List<GuaranteeDTO> guarantees = guaranteeService.getAllGuarantees();
        return ResponseEntity.ok(guarantees);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get guarantee by ID")
    public ResponseEntity<GuaranteeDTO> getGuaranteeById(@PathVariable Long id) {
        return guaranteeService.getGuaranteeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a guarantee")
    public ResponseEntity<GuaranteeDTO> updateGuarantee(@PathVariable Long id, @RequestBody GuaranteeDTO dto) {
        try {
            GuaranteeDTO updated = guaranteeService.updateGuarantee(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a guarantee")
    public ResponseEntity<Void> deleteGuarantee(@PathVariable Long id) {
        guaranteeService.deleteGuarantee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue a guarantee")
    public ResponseEntity<GuaranteeDTO> issueGuarantee(@PathVariable Long id) {
        try {
            GuaranteeDTO issued = guaranteeService.issueGuarantee(id);
            return ResponseEntity.ok(issued);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/amend")
    @Operation(summary = "Amend a guarantee")
    public ResponseEntity<AmendmentDTO> amendGuarantee(@PathVariable Long id, @RequestBody AmendmentDTO dto) {
        try {
            AmendmentDTO amendment = guaranteeService.amendGuarantee(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(amendment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Submit a claim for a guarantee")
    public ResponseEntity<ClaimDTO> submitClaim(@PathVariable Long id, @RequestBody ClaimDTO dto) {
        try {
            ClaimDTO claim = guaranteeService.submitClaim(id, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(claim);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get guarantees by status")
    public ResponseEntity<List<GuaranteeDTO>> getGuaranteesByStatus(@PathVariable String status) {
        try {
            List<GuaranteeDTO> guarantees = guaranteeService.getGuaranteesByStatus(status);
            return ResponseEntity.ok(guarantees);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/amendments")
    @Operation(summary = "Get amendments for a guarantee")
    public ResponseEntity<List<AmendmentDTO>> getAmendments(@PathVariable Long id) {
        List<AmendmentDTO> amendments = guaranteeService.getAmendmentsForGuarantee(id);
        return ResponseEntity.ok(amendments);
    }

    @GetMapping("/{id}/claims")
    @Operation(summary = "Get claims for a guarantee")
    public ResponseEntity<List<ClaimDTO>> getClaims(@PathVariable Long id) {
        List<ClaimDTO> claims = guaranteeService.getClaimsForGuarantee(id);
        return ResponseEntity.ok(claims);
    }
}
