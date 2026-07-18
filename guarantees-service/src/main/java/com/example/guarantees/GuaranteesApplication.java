package com.example.guarantees;

import com.example.guarantees.service.ExportJobStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class GuaranteesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuaranteesApplication.class, args);
    }

    @Bean
    public ExportJobStore exportJobStore() {
        return new ExportJobStore();
    }

}
