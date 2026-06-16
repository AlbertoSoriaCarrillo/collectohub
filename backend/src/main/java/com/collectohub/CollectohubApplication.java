package com.collectohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CollectohubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectohubApplication.class, args);
    }
}
