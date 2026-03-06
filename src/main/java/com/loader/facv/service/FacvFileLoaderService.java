package com.loader.facv.service;

import com.loader.facv.LoaderProperties;
import com.loader.facv.model.FacvRecord;
import com.loader.facv.repository.FacvRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacvFileLoaderService {

    private final LoaderProperties properties;
    private final FixedWidthFacvParser parser;
    private final FacvRecordRepository repository;

    public long loadAllConfiguredFiles() {
        if (!properties.isEnabled()) {
            log.info("Loader disabled via loader.enabled=false");
            return 0L;
        }

        List<String> fileNames = properties.getFileNames();
        if (fileNames == null || fileNames.isEmpty()) {
            log.warn("No file names configured under loader.file-names");
            return 0L;
        }

        Path basePath = Paths.get(properties.getInputDirectory());
        log.info("FACV load started from directory: {}", basePath.toAbsolutePath());
        log.info("Expected fixed-width map: {}", parser.expectedColumnLengths());

        long totalInserted = 0L;
        for (String fileName : fileNames) {
            Path filePath = basePath.resolve(fileName);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("File not found or not regular file, skipping: {}", filePath.toAbsolutePath());
                continue;
            }

            totalInserted += loadSingleFile(filePath);
        }
        log.info("FACV load completed. Total rows inserted: {}", totalInserted);
        return totalInserted;
    }

    private long loadSingleFile(Path filePath) {
        Charset charset = Charset.forName(properties.getCharset());
        int batchSize = Math.max(properties.getBatchSize(), 1);
        List<FacvRecord> buffer = new ArrayList<FacvRecord>(batchSize);

        long insertedRows = 0L;
        long lineNumber = 0L;
        long skippedBlankLines = 0L;

        log.info("Processing file: {}", filePath.toAbsolutePath());
        try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    skippedBlankLines++;
                    continue;
                }

                FacvRecord record = parser.parse(line);
                buffer.add(record);

                if (buffer.size() >= batchSize) {
                    repository.batchInsert(buffer);
                    insertedRows += buffer.size();
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                repository.batchInsert(buffer);
                insertedRows += buffer.size();
                buffer.clear();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read file: " + filePath.toAbsolutePath(), ex);
        }

        log.info("Completed file: {} | lines read: {} | inserted: {} | skipped blank: {}",
                filePath.getFileName(), lineNumber, insertedRows, skippedBlankLines);
        return insertedRows;
    }
}
