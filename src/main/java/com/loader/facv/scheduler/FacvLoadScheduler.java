package com.loader.facv.scheduler;

import com.loader.facv.service.FacvFileLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacvLoadScheduler {

    private final FacvFileLoaderService loaderService;

    @Scheduled(cron = "${loader.schedule-cron}", zone = "${loader.schedule-zone}")
    public void runDailyLoad() {
        log.info("Scheduler triggered FACV load job");
        loaderService.loadAllConfiguredFiles();
    }
}
