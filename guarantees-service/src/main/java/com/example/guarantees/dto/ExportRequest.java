package com.example.guarantees.dto;

import java.util.HashMap;
import java.util.Map;

public class ExportRequest {
    private String format; // xlsx or csv
    private Map<String, String> filters = new HashMap<>();
    private String sortBy;
    private String sortDirection;

    public ExportRequest() {
    }

    public ExportRequest(String format, Map<String, String> filters, String sortBy, String sortDirection) {
        this.format = format;
        this.filters = filters;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
