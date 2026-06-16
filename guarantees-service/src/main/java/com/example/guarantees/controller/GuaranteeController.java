package com.example.guarantees.controller;

import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.dto.AmendmentRequest;
import com.example.guarantees.dto.ClaimDTO;
import com.example.guarantees.dto.ClaimRequest;
import com.example.guarantees.dto.CreateGuaranteeRequest;
import com.example.guarantees.dto.GuaranteeDTO;
import com.example.guarantees.service.GuaranteeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guarantees")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class GuaranteeController {

    private final GuaranteeService service;

    public GuaranteeController(GuaranteeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<GuaranteeDTO>> getAllGuarantees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        GuaranteeStatus statusEnum = (status != null && !status.isBlank())
                ? GuaranteeStatus.valueOf(status) : null;
        GuaranteeType typeEnum = (type != null && !type.isBlank())
                ? GuaranteeType.valueOf(type) : null;
        return ResponseEntity.ok(service.findAll(statusEnum, typeEnum));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuaranteeDTO> getGuarantee(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<GuaranteeDTO> createGuarantee(@RequestBody CreateGuaranteeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuaranteeDTO> updateGuarantee(@PathVariable Long id,
                                                         @RequestBody CreateGuaranteeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuarantee(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<GuaranteeDTO> issueGuarantee(@PathVariable Long id) {
        return ResponseEntity.ok(service.issue(id));
    }

    @PostMapping("/{id}/amendments")
    public ResponseEntity<GuaranteeDTO> addAmendment(@PathVariable Long id,
                                                      @RequestBody AmendmentRequest request) {
        return ResponseEntity.ok(service.addAmendment(
                id, request.getNewAmount(), request.getNewExpiryDate(), request.getDescription()));
    }

    @PostMapping("/{id}/claims")
    public ResponseEntity<GuaranteeDTO> addClaim(@PathVariable Long id,
                                                  @RequestBody ClaimRequest request) {
        return ResponseEntity.ok(service.addClaim(id, request.getClaimedAmount(), request.getReason()));
    }

    @GetMapping("/{id}/claims")
    public ResponseEntity<List<ClaimDTO>> listClaims(@PathVariable Long id) {
        return ResponseEntity.ok(service.listClaims(id));
    }
}
