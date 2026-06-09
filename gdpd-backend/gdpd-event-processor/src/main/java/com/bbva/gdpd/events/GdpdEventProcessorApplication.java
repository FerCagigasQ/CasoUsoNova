package com.bbva.gdpd.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class GdpdEventProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(GdpdEventProcessorApplication.class, args);
    }
}
