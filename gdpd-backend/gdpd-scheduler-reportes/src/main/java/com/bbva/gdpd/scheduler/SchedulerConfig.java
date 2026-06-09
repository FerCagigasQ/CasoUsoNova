package com.bbva.gdpd.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SchedulerConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
