package com.loader.facv;

import com.loader.facv.service.FacvFileLoaderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

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
                loaderService.loadAllConfiguredFiles();
            }
        };
    }
}
