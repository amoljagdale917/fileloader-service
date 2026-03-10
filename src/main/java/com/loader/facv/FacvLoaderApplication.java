package com.loader.facv;

import com.loader.facv.service.FacvFileLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class FacvLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacvLoaderApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            FacvFileLoaderService loaderService,
            @Value("${loader.run-on-startup:false}") boolean runOnStartup
    ) {
        return args -> {
            if (runOnStartup) {
                try {
                    log.info("Startup load is enabled. Running FACV load job.");
                    long insertedRows = loaderService.loadAllConfiguredFiles();
                    log.info("Startup FACV load completed. insertedRows={}", insertedRows);
                } catch (RuntimeException ex) {
                    log.error("Startup FACV load failed.", ex);
                }
            }
        };
    }
}
