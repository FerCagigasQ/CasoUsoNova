package com.example.guarantees.controller;

import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.dto.AmendmentRequest;
import com.example.guarantees.dto.ClaimDTO;
import com.example.guarantees.dto.ClaimRequest;
import com.example.guarantees.dto.CreateGuaranteeRequest;
import com.example.guarantees.dto.ExpiryCalendarDTO;
import com.example.guarantees.dto.GuaranteeDTO;
import com.example.guarantees.service.ExpiryCalendarService;
import com.example.guarantees.service.GuaranteeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/guarantees")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Tag(name = "Guarantees", description = "Bank guarantee lifecycle (ICC URDG 758): CRUD, issue, amendments, claims and expiry calendar")
public class GuaranteeController {

    private final GuaranteeService service;
    private final ExpiryCalendarService expiryCalendarService;

    public GuaranteeController(GuaranteeService service, ExpiryCalendarService expiryCalendarService) {
        this.service = service;
        this.expiryCalendarService = expiryCalendarService;
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

    @GetMapping("/expiry-calendar")
    @Operation(
        summary = "Get monthly expiry calendar",
        description = """
            Returns the guarantees expiring in the given month, grouped by day, with per-day totals \
            (overall and per currency) and a risk level per guarantee and per day.

            Risk catalog (riskLevel / aggregateRiskLevel), computed against today's date and the guarantee amount:
            - `none`: no expiries that day (only used at day level)
            - `low`: expires in more than 30 days AND amount < 50,000
            - `medium`: expires in 8-30 days OR amount between 50,000 and 200,000
            - `high`: expires in 1-7 days OR amount between 200,000 and 1,000,000
            - `critical`: expires today / already expired OR amount > 1,000,000

            The day-level `aggregateRiskLevel` is the highest risk among the guarantees expiring that day. \
            CANCELLED guarantees are excluded.

            Note (SSE): when the scheduler auto-expires a guarantee, the event stream `GET /api/v1/guarantees/events` \
            emits an event named `guarantee-events` with `eventType: "expiration-auto"` — see the Guarantee Events tag."""
    )
    @ApiResponse(
        responseCode = "200",
        description = "Calendar for the requested month",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                  "month": "2026-07",
                  "days": [
                    {
                      "day": 20,
                      "guarantees": [
                        {
                          "id": 1,
                          "reference": "CAL-001",
                          "beneficiary": { "firstName": "Li", "lastName": "Wei" },
                          "amount": 250000.00,
                          "currency": "USD",
                          "expiryDate": "2026-07-20",
                          "daysUntilExpiry": 2,
                          "riskLevel": "high"
                        }
                      ],
                      "totalByAmount": 250000.00,
                      "totalByCurrency": { "USD": 250000.00 },
                      "aggregateRiskLevel": "high"
                    }
                  ],
                  "riskCatalog": {
                    "none": "No vencimientos ese dia",
                    "low": ">30 dias y < 50k",
                    "medium": "8-30 dias o importe entre 50k y 200k",
                    "high": "1-7 dias o importe entre 200k y 1M",
                    "critical": "Vence hoy/ya vencio o importe > 1M"
                  }
                }
                """)
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid month format (expected YYYY-MM)",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{ \"error\": \"Text '2026-13' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13\" }")
        )
    )
    public ResponseEntity<ExpiryCalendarDTO> getExpiryCalendar(
            @Parameter(description = "Calendar month in ISO-8601 format YYYY-MM", required = true,
                       example = "2026-07", schema = @Schema(type = "string", pattern = "^\\d{4}-\\d{2}$"))
            @RequestParam String month) {
        return ResponseEntity.ok(expiryCalendarService.getMonthlyCalendar(YearMonth.parse(month)));
    }
}
