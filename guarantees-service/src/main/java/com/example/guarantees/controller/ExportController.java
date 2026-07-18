package com.example.guarantees.controller;

import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.dto.ExportRequest;
import com.example.guarantees.dto.ExportResponse;
import com.example.guarantees.service.ExportJobStore;
import com.example.guarantees.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Tag(name = "Export", description = "Async CSV/Excel export jobs for bank guarantees")
@RestController
@RequestMapping("/api/v1/guarantees/export")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
public class ExportController {

    private final ExportService exportService;
    private final ExportJobStore jobStore;

    public ExportController(ExportService exportService, ExportJobStore jobStore) {
        this.exportService = exportService;
        this.jobStore = jobStore;
    }

    @Operation(summary = "Start an async export job",
               description = "Accepts format (xlsx or csv) and optional filters. Returns 202 with a jobId immediately.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Job accepted",
                     content = @Content(schema = @Schema(implementation = ExportResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid format or filter value")
    })
    @PostMapping
    public ResponseEntity<ExportResponse> startExport(@RequestBody ExportRequest request) {
        // Validate format
        if (request.getFormat() == null ||
            (!request.getFormat().equalsIgnoreCase("xlsx") &&
             !request.getFormat().equalsIgnoreCase("csv"))) {
            return ResponseEntity.badRequest().build();
        }

        // Generate job ID
        String jobId = exportService.generateJobId();

        // Create job info
        ExportJobStore.JobInfo jobInfo = new ExportJobStore.JobInfo(jobId);
        jobStore.put(jobId, jobInfo);

        // Parse filters
        GuaranteeStatus status = null;
        GuaranteeType type = null;

        if (request.getFilters() != null) {
            if (request.getFilters().containsKey("status")) {
                try {
                    status = GuaranteeStatus.valueOf(request.getFilters().get("status"));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }
            if (request.getFilters().containsKey("type")) {
                try {
                    type = GuaranteeType.valueOf(request.getFilters().get("type"));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }
        }

        // Start async export
        exportService.exportToExcel(jobId, request.getFormat(), status, type,
                                   request.getSortBy(), request.getSortDirection());

        // Return 202 Accepted
        ExportResponse response = new ExportResponse(jobId, "processing", 0,
                                                     "Export job started", jobInfo.createdAt);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "Get export job status",
               description = "Returns current job state. When status=completed, includes downloadUrl.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job status",
                     content = @Content(schema = @Schema(implementation = ExportResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<?> getExportStatus(
            @Parameter(description = "Job ID returned by the POST endpoint") @PathVariable String jobId) {
        if (!jobStore.exists(jobId)) {
            return ResponseEntity.notFound().build();
        }

        ExportJobStore.JobInfo jobInfo = jobStore.get(jobId);

        // If completed and download requested (by checking if file exists)
        if ("completed".equals(jobInfo.status) && jobInfo.fileData != null) {
            // Return download URL in response
            ExportResponse response = new ExportResponse(jobInfo.jobId, jobInfo.status,
                                                        jobInfo.progress, jobInfo.message,
                                                        jobInfo.createdAt);
            response.setDownloadUrl("/api/v1/guarantees/export/" + jobId + "/download");
            return ResponseEntity.ok(response);
        }

        // Return status response
        ExportResponse response = new ExportResponse(jobInfo.jobId, jobInfo.status,
                                                     jobInfo.progress, jobInfo.message,
                                                     jobInfo.createdAt);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download the generated file",
               description = "Streams the xlsx or csv binary. Returns 409 if job is still processing.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File binary (xlsx or csv)",
                     content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "409", description = "Job not yet completed")
    })
    @GetMapping("/{jobId}/download")
    public ResponseEntity<Resource> downloadExport(
            @Parameter(description = "Job ID returned by the POST endpoint") @PathVariable String jobId) {
        if (!jobStore.exists(jobId)) {
            return ResponseEntity.notFound().build();
        }

        ExportJobStore.JobInfo jobInfo = jobStore.get(jobId);

        if (!"completed".equals(jobInfo.status) || jobInfo.fileData == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        File file = jobInfo.fileData;
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String filename = "guarantees-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) +
                         (file.getName().endsWith(".csv") ? ".csv" : ".xlsx");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename(filename)
                .build()
        );

        if (file.getName().endsWith(".xlsx")) {
            headers.setContentType(
                org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
        } else {
            headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
