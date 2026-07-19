package com.example.guarantees.dto;

import java.util.List;
import java.util.Map;

public record ExpiryCalendarDTO(
        String month,
        List<ExpiryCalendarDayDTO> days,
        Map<String, String> riskCatalog) {
}
