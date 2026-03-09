package com.loader.facv;

import com.loader.facv.service.FacvFileLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(LoaderProperties.class)
public class FacvLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacvLoaderApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(FacvFileLoaderService loaderService, LoaderProperties properties) {
        return args -> {
            if (properties.isRunOnStartup()) {
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
