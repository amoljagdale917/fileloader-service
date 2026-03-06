package com.loader.facv.scheduler;

import com.loader.facv.service.FacvFileLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FacvLoadScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacvLoadScheduler.class);

    private final FacvFileLoaderService loaderService;

    public FacvLoadScheduler(FacvFileLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @Scheduled(cron = "${loader.schedule-cron}", zone = "${loader.schedule-zone}")
    public void runDailyLoad() {
        LOGGER.info("Scheduler triggered FACV load job");
        loaderService.loadAllConfiguredFiles();
    }
}
