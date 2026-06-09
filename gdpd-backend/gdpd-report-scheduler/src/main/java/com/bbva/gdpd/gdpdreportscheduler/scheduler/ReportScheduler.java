package com.bbva.gdpd.gdpdreportscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ReportScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ReportScheduler.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gdpd.batch.service-url:http://localhost:8082}")
    private String batchServiceUrl;

    @Scheduled(cron = "0 0 2 * * ?")
    public void triggerReportBatch() {
        LOG.info("[gdpd-report-scheduler] Lanzando batch de reportes GDPD...");
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    batchServiceUrl + "/batch/run", null, String.class);
            LOG.info("[gdpd-report-scheduler] Batch lanzado. Status={} Body={}",
                    resp.getStatusCode(), resp.getBody());
        } catch (RestClientException e) {
            LOG.error("[gdpd-report-scheduler] Error al lanzar el batch: {}", e.getMessage());
        }
    }
}
