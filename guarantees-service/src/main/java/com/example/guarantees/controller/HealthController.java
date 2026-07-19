package com.example.guarantees.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Tag(name = "Health", description = "Lightweight API health alias (full details at /actuator/health)")
public class HealthController {

    @GetMapping
    @Operation(summary = "API health check", description = "Returns 200 with {\"status\":\"UP\"} when the API is up. Actuator exposes the detailed health at /actuator/health.")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
