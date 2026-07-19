package com.example.guarantees.controller;

import com.example.guarantees.service.GuaranteeEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/guarantees")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Tag(name = "Guarantee Events", description = "Server-Sent Events stream with live guarantee changes")
public class GuaranteeEventController {

    private final GuaranteeEventService guaranteeEventService;

    public GuaranteeEventController(GuaranteeEventService guaranteeEventService) {
        this.guaranteeEventService = guaranteeEventService;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Subscribe to the guarantee event stream (SSE)",
        description = """
            Opens a Server-Sent Events connection (30 min timeout; the browser `EventSource` reconnects automatically). \
            Consume it with `new EventSource('/api/v1/guarantees/events')` and `addEventListener` per event name.

            Named events emitted on the stream:

            1. `guarantee-change` — any lifecycle change (CREATED, UPDATED, ISSUED, AMENDED, CLAIMED, DELETED, EXPIRED_AUTO):
            ```
            event: guarantee-change
            data: {"guaranteeId": 1, "action": "ISSUED", "reference": "REF-001", "status": "ISSUED", "occurredAt": "2026-07-19T12:00:00Z"}
            ```

            2. `guarantee-events` — channel for enriched events; the payload discriminator is `eventType`. \
            Currently emitted with `eventType: "expiration-auto"` when the scheduler auto-expires a guarantee \
            (ISSUED/AMENDED with expiryDate <= today, checked every 30s in the demo profile):
            ```
            event: guarantee-events
            data: {"eventType": "expiration-auto", "guaranteeId": "1", "reference": "REF-001", "status": "EXPIRED", "expiryDate": "2026-07-01", "reason": "Auto-expired by scheduler", "expiredAt": "2026-07-19T12:00:00Z"}
            ```

            CORS: the endpoint allows origins http://localhost (frontend :80) and http://localhost:4200, so a \
            cross-origin `EventSource` works without extra configuration (SSE uses a simple GET, no preflight)."""
    )
    @ApiResponse(
        responseCode = "200",
        description = "SSE stream established",
        content = @Content(
            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
            examples = @ExampleObject(value = """
                event: guarantee-events
                data: {"eventType":"expiration-auto","guaranteeId":"1","reference":"REF-001","status":"EXPIRED","expiryDate":"2026-07-01","reason":"Auto-expired by scheduler","expiredAt":"2026-07-19T12:00:00Z"}
                """)
        )
    )
    public SseEmitter subscribe() {
        return guaranteeEventService.subscribe();
    }
}
