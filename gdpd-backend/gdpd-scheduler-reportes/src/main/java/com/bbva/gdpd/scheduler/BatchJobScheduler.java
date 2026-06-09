package com.bbva.gdpd.scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Component
public class BatchJobScheduler {
    private static final Logger log = LoggerFactory.getLogger(BatchJobScheduler.class);
    private final RestTemplate restTemplate;
    @Value("${gdpd.batch.service.url:http://gdpd-batch-reportes}")
    private String batchServiceUrl;
    public BatchJobScheduler(RestTemplate restTemplate) { this.restTemplate = restTemplate; }
    @Scheduled(cron = "${gdpd.batch.reportes.cron:0 0 1 * * MON-FRI}")
    public void ejecutarGeneracionReportes() {
        try {
            log.info("Disparando generarReportesJob en {}", batchServiceUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    batchServiceUrl + "/api/batch/ejecutar", null, String.class);
            log.info("Job disparado - respuesta HTTP {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Error al lanzar job de reportes: {}", e.getMessage(), e);
        }
    }
}
