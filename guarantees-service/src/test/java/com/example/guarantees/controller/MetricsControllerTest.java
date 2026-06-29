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
            .andExpect(jsonPath("$.byCurrency").isMap())
            .andExpect(jsonPath("$.byMonth").isArray())
            .andExpect(jsonPath("$.totalAmount").isNumber())
            .andExpect(jsonPath("$.averageAmount").isNumber())
            .andExpect(jsonPath("$.activeCount").isNumber())
            .andExpect(jsonPath("$.expiringIn30Days").isNumber());
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

    @Test
    void getMetrics_filtersByStatusTypeAndCurrency() throws Exception {
        mockMvc.perform(get("/api/v1/metrics")
                .param("status", "ISSUED")
                .param("type", "PERFORMANCE")
                .param("currency", "usd")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.byStatus").isEmpty())
            .andExpect(jsonPath("$.byType").isEmpty())
            .andExpect(jsonPath("$.byCurrency").isEmpty())
            .andExpect(jsonPath("$.totalAmount").value(0.00));
    }

    @Test
    void getMetrics_filtersByIssueDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/metrics")
                .param("issueDateFrom", "2024-01-01")
                .param("issueDateTo", "2024-03-31")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.byMonth[0].month").value("2024-01"))
            .andExpect(jsonPath("$.byMonth[2].month").value("2024-03"));
    }
}
