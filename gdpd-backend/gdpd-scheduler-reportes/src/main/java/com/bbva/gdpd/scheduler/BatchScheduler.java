package com.bbva.gdpd.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final RestTemplate restTemplate;

    @Value("${batch.service.url:http://gdpd-batch-reportes:8083}")
    private String batchServiceUrl;

    public BatchScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron = "${batch.scheduler.cron:0 0 2 * * MON-FRI}")
    public void lanzarReportePedidos() {
        log.info("Iniciando lanzamiento programado del job reportePedidosJob");
        try {
            String url = batchServiceUrl + "/actuator/batch/jobs/reportePedidosJob";
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            log.info("Job lanzado exitosamente. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error lanzando job reportePedidosJob: {}", e.getMessage());
        }
    }
}
