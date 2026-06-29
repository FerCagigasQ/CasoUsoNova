package com.example.guarantees.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMetrics_returnsValidStructure() throws Exception {
        mockMvc.perform(get("/api/v1/metrics").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").isNumber())
            .andExpect(jsonPath("$.byStatus").isMap())
            .andExpect(jsonPath("$.byType").isMap())
            .andExpect(jsonPath("$.byMonth").isArray());
    }

    @Test
    void getMetrics_totalMatchesSumOfByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/metrics").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(6));
    }

    @Test
    void getMetrics_byMonthIsSorted() throws Exception {
        mockMvc.perform(get("/api/v1/metrics").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.byMonth[0].month").value("2024-01"))
            .andExpect(jsonPath("$.byMonth[0].count").value(1));
    }
}
