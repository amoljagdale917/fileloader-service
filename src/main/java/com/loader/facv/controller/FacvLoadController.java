package com.loader.facv.controller;

import com.loader.facv.service.FacvFileLoaderService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/facv")
public class FacvLoadController {

    private final FacvFileLoaderService loaderService;

    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> triggerLoad() {
        log.info("Manual FACV load triggered via REST endpoint");
        long insertedRows = loaderService.loadAllConfiguredFiles();
        log.info("Manual FACV load completed. insertedRows={}", insertedRows);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", "SUCCESS");
        body.put("insertedRows", insertedRows);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health endpoint invoked");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", "UP");
        body.put("service", "facv-loader-service");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
