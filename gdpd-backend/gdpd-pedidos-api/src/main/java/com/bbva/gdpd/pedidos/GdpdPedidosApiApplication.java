package com.bbva.gdpd.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class GdpdPedidosApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GdpdPedidosApiApplication.class, args);
    }
}
