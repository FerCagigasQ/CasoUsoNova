package com.example.guarantees.dto;

public class MonthlyCountDTO {
    private String month;
    private Long count;

    public MonthlyCountDTO(String month, Long count) {
        this.month = month;
        this.count = count;
    }

    public String getMonth() { return month; }
    public Long getCount() { return count; }
}
