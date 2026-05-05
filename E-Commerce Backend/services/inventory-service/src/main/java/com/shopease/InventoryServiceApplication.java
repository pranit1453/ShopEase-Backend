package com.shopease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class InventoryServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

}
