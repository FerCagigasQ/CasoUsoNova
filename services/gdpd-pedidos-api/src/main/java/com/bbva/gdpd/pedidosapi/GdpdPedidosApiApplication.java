package com.bbva.gdpd.pedidosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GdpdPedidosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdpdPedidosApiApplication.class, args);
    }
}
