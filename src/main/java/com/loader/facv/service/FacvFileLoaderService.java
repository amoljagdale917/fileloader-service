package com.loader.facv.service;

import com.loader.facv.LoaderProperties;
import com.loader.facv.model.FacvRecord;
import com.loader.facv.repository.FacvRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacvFileLoaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacvFileLoaderService.class);

    private final LoaderProperties properties;
    private final FixedWidthFacvParser parser;
    private final FacvRecordRepository repository;

    public FacvFileLoaderService(LoaderProperties properties,
                                 FixedWidthFacvParser parser,
                                 FacvRecordRepository repository) {
        this.properties = properties;
        this.parser = parser;
        this.repository = repository;
    }

    public long loadAllConfiguredFiles() {
        if (!properties.isEnabled()) {
            LOGGER.info("Loader disabled via loader.enabled=false");
            return 0L;
        }

        List<String> fileNames = properties.getFileNames();
        if (fileNames == null || fileNames.isEmpty()) {
            LOGGER.warn("No file names configured under loader.file-names");
            return 0L;
        }

        Path basePath = Paths.get(properties.getInputDirectory());
        LOGGER.info("FACV load started from directory: {}", basePath.toAbsolutePath());
        LOGGER.info("Expected fixed-width map: {}", parser.expectedColumnLengths());

        long totalInserted = 0L;
        for (String fileName : fileNames) {
            Path filePath = basePath.resolve(fileName);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                LOGGER.warn("File not found or not regular file, skipping: {}", filePath.toAbsolutePath());
                continue;
            }

            totalInserted += loadSingleFile(filePath);
        }
        LOGGER.info("FACV load completed. Total rows inserted: {}", totalInserted);
        return totalInserted;
    }

    private long loadSingleFile(Path filePath) {
        Charset charset = Charset.forName(properties.getCharset());
        int batchSize = Math.max(properties.getBatchSize(), 1);
        List<FacvRecord> buffer = new ArrayList<FacvRecord>(batchSize);

        long insertedRows = 0L;
        long lineNumber = 0L;
        long skippedBlankLines = 0L;

        LOGGER.info("Processing file: {}", filePath.toAbsolutePath());
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

        LOGGER.info("Completed file: {} | lines read: {} | inserted: {} | skipped blank: {}",
                filePath.getFileName(), lineNumber, insertedRows, skippedBlankLines);
        return insertedRows;
    }
}
