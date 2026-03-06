package com.loader.facv.controller;

import com.loader.facv.service.FacvFileLoaderService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facv")
public class FacvLoadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacvLoadController.class);

    private final FacvFileLoaderService loaderService;

    public FacvLoadController(FacvFileLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> triggerLoad() {
        LOGGER.info("Manual FACV load triggered via REST endpoint");
        long insertedRows = loaderService.loadAllConfiguredFiles();
        LOGGER.info("Manual FACV load completed. insertedRows={}", insertedRows);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", "SUCCESS");
        body.put("insertedRows", insertedRows);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        LOGGER.debug("Health endpoint invoked");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", "UP");
        body.put("service", "facv-loader-service");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
