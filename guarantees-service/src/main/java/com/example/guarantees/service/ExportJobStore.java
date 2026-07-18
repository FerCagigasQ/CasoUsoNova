package com.example.guarantees.service;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class ExportJobStore {

    public static class JobInfo {
        public String jobId;
        public String status; // processing, completed, failed
        public Integer progress;
        public String message;
        public File fileData;
        public Throwable error;
        public Instant createdAt;

        public JobInfo(String jobId) {
            this.jobId = jobId;
            this.status = "processing";
            this.progress = 0;
            this.message = "Initializing export...";
            this.createdAt = Instant.now();
        }
    }

    private final ConcurrentHashMap<String, JobInfo> jobs = new ConcurrentHashMap<>();

    public void put(String jobId, JobInfo jobInfo) {
        jobs.put(jobId, jobInfo);
    }

    public JobInfo get(String jobId) {
        return jobs.get(jobId);
    }

    public boolean exists(String jobId) {
        return jobs.containsKey(jobId);
    }

    public void remove(String jobId) {
        jobs.remove(jobId);
    }

    public void updateProgress(String jobId, Integer progress, String message) {
        JobInfo job = jobs.get(jobId);
        if (job != null) {
            job.progress = progress;
            job.message = message;
        }
    }

    public void markCompleted(String jobId, File file) {
        JobInfo job = jobs.get(jobId);
        if (job != null) {
            job.status = "completed";
            job.progress = 100;
            job.message = "Export completed successfully";
            job.fileData = file;
        }
    }

    public void markFailed(String jobId, Throwable error) {
        JobInfo job = jobs.get(jobId);
        if (job != null) {
            job.status = "failed";
            job.message = "Export failed: " + error.getMessage();
            job.error = error;
        }
    }
}
