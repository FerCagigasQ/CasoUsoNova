package com.example.guarantees.dto;

import java.time.Instant;

public record GuaranteeChangeEventDTO(
        Long guaranteeId,
        String action,
        String reference,
        String status,
        Instant occurredAt
) {
}
