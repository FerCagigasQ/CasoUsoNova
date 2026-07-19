package com.example.guarantees.controller;

import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.dto.ExportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startExport_returnsAcceptedWithJobId() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "ISSUED");
        request.setFilters(filters);

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").exists())
            .andExpect(jsonPath("$.status").value("processing"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void startExport_invalidFormat_returnsBadRequest() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("pdf");

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void startExport_invalidStatus_returnsBadRequest() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "INVALID_STATUS");
        request.setFilters(filters);

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void startExport_csvFormat_returnsAccepted() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("csv");

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void getExportStatus_nonExistentJob_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/guarantees/export/nonexistent-job")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void getExportStatus_processingJob_returnsStatus() throws Exception {
        // Start export
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");

        var result = mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andReturn();

        // Extract jobId from response
        String responseBody = result.getResponse().getContentAsString();
        String jobId = objectMapper.readTree(responseBody).get("jobId").asText();

        // Get status immediately (should be processing)
        mockMvc.perform(get("/api/v1/guarantees/export/" + jobId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(jobId))
            .andExpect(jsonPath("$.status").value("processing"))
            .andExpect(jsonPath("$.progress").isNumber());
    }

    @Test
    void downloadExport_nonExistentJob_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/guarantees/export/nonexistent-job/download")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void downloadExport_processingJob_returnsConflict() throws Exception {
        // Start export
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");

        var result = mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andReturn();

        // Extract jobId from response
        String responseBody = result.getResponse().getContentAsString();
        String jobId = objectMapper.readTree(responseBody).get("jobId").asText();

        // Try to download while processing (should fail)
        mockMvc.perform(get("/api/v1/guarantees/export/" + jobId + "/download"))
            .andExpect(status().isConflict());
    }

    @Test
    void startExport_withFiltersAndSorting_returnsAccepted() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");

        Map<String, String> filters = new HashMap<>();
        filters.put("status", "ISSUED");
        filters.put("type", "PERFORMANCE");
        request.setFilters(filters);
        request.setSortBy("issueDate");
        request.setSortDirection("desc");

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").exists());
    }

    @Test
    void startExport_withoutFilters_returnsAccepted() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setFormat("xlsx");

        mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void getExportStatus_returnsJobInfo() throws Exception {
        // Start export
        ExportRequest request = new ExportRequest();
        request.setFormat("csv");

        var result = mockMvc.perform(post("/api/v1/guarantees/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String jobId = objectMapper.readTree(responseBody).get("jobId").asText();

        // Get status
        mockMvc.perform(get("/api/v1/guarantees/export/" + jobId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(jobId))
            .andExpect(jsonPath("$.status").value(org.hamcrest.Matchers.isIn(
                java.util.List.of("processing", "completed"))))
            .andExpect(jsonPath("$.message").exists());
    }
}
