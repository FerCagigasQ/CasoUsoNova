package com.bbva.gdpd.gdpdreportbatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batch")
public class BatchJobController {
    private static final Logger LOG = LoggerFactory.getLogger(BatchJobController.class);
    private final JobLauncher jobLauncher;
    private final Job generarReporteJob;

    public BatchJobController(JobLauncher jobLauncher, Job generarReporteJob) {
        this.jobLauncher = jobLauncher;
        this.generarReporteJob = generarReporteJob;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runReporteJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(generarReporteJob, params);
            LOG.info("Job gdpdreportbatch lanzado");
            return ResponseEntity.ok("Job gdpdreportbatch iniciado");
        } catch (Exception e) {
            LOG.error("Error al lanzar el job", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
