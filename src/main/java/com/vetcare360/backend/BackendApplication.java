package com.vetcare360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.vetcare360.backend", "com.vetcare360.products"})
@EnableJpaRepositories(basePackages = "com.vetcare360.products")
@EntityScan(basePackages = "com.vetcare360.products.entity")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}