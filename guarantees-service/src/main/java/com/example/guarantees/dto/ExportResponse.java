package com.example.guarantees.dto;

import java.time.Instant;

public class ExportResponse {
    private String jobId;
    private String status; // processing, completed, failed
    private Integer progress; // 0-100
    private String message;
    private String downloadUrl;
    private Instant createdAt;

    public ExportResponse() {
    }

    public ExportResponse(String jobId, String status, Integer progress, String message, Instant createdAt) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
