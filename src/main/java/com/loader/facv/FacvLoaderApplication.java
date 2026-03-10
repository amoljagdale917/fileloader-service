package com.loader.facv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FacvLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacvLoaderApplication.class, args);
    }
}
