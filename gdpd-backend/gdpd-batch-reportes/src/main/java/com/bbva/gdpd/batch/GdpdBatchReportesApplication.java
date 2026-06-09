package com.bbva.gdpd.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class GdpdBatchReportesApplication {
    public static void main(String[] args) {
        SpringApplication.run(GdpdBatchReportesApplication.class, args);
    }
}
