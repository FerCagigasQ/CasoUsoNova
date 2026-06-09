package com.bbva.gdpd.batch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchTriggerController {

    private static final Logger log = LoggerFactory.getLogger(BatchTriggerController.class);

    private final JobLauncher jobLauncher;
    private final Job generarReportesJob;

    public BatchTriggerController(JobLauncher jobLauncher, Job generarReportesJob) {
        this.jobLauncher = jobLauncher;
        this.generarReportesJob = generarReportesJob;
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<String> ejecutar() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(generarReportesJob, params);
            log.info("Job generarReportesJob lanzado manualmente");
            return ResponseEntity.ok("Job lanzado correctamente");
        } catch (Exception e) {
            log.error("Error lanzando job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
