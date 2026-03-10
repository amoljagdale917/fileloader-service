package com.loader.facv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.loader.facv.model.FacvRecord;
import com.loader.facv.repository.FacvRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacvFileLoaderService {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("HHmmssSSS");

    private final FixedWidthFacvParser parser;
    private final FacvRecordRepository repository;
    @Value("${loader.enabled:true}")
    private boolean enabled;
    @Value("${loader.incoming-path:hub/var/incoming}")
    private String incomingPath;
    @Value("${loader.success-path:hub/var/success}")
    private String successPath;
    @Value("${loader.failed-path:hub/var/failed}")
    private String failedPath;
    @Value("${loader.file-names:}")
    private List<String> fileNames = new ArrayList<String>();
    @Value("${loader.charset:UTF-8}")
    private String charset;
    @Value("${loader.batch-size:1000}")
    private int batchSize;

    public long loadAllConfiguredFiles() {
        if (!enabled) {
            log.info("Loader disabled via loader.enabled=false");
            return 0L;
        }

        List<String> configuredFileNames = fileNames;
        if (configuredFileNames == null || configuredFileNames.isEmpty()) {
            log.warn("No file names configured under loader.file-names");
            return 0L;
        }

        Path incomingPath = Paths.get(this.incomingPath);
        Path successPath = Paths.get(this.successPath);
        Path failedPath = Paths.get(this.failedPath);
        ensureDirectory(incomingPath);
        ensureDirectory(successPath);
        ensureDirectory(failedPath);

        log.info("FACV load started from incoming directory: {}", incomingPath.toAbsolutePath());
        log.info("Success directory: {}", successPath.toAbsolutePath());
        log.info("Failed directory: {}", failedPath.toAbsolutePath());
        log.info("Expected fixed-width map: {}", parser.expectedColumnLengths());

        long totalInserted = 0L;
        for (String fileName : configuredFileNames) {
            Path filePath = incomingPath.resolve(fileName);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("File not found or not regular file, skipping: {}", filePath.toAbsolutePath());
                continue;
            }

            try {
                long inserted = loadSingleFile(filePath);
                totalInserted += inserted;
                Path movedTo = moveFile(filePath, successPath);
                log.info("File moved to success: {}", movedTo.getFileName());
            } catch (RuntimeException ex) {
                log.error("Processing failed for file: {}. Moving to failed folder.", fileName, ex);
                moveToFailedSafely(filePath, failedPath);
            }
        }
        log.info("FACV load completed. Total rows inserted: {}", totalInserted);
        return totalInserted;
    }

    private long loadSingleFile(Path filePath) {
        Charset charset = Charset.forName(this.charset);
        int batchSize = Math.max(this.batchSize, 1);
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

    private void ensureDirectory(Path dirPath) {
        try {
            Files.createDirectories(dirPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create/access directory: " + dirPath.toAbsolutePath(), ex);
        }
    }

    private void moveToFailedSafely(Path sourceFile, Path failedPath) {
        if (!Files.exists(sourceFile)) {
            log.warn("Unable to move to failed folder. Source file no longer exists: {}", sourceFile.toAbsolutePath());
            return;
        }
        try {
            Path movedTo = moveFile(sourceFile, failedPath);
            log.info("File moved to failed: {}", movedTo.getFileName());
        } catch (RuntimeException moveEx) {
            log.error("Failed to move file to failed folder: {}", sourceFile.toAbsolutePath(), moveEx);
        }
    }

    private Path moveFile(Path sourceFile, Path targetDirectory) {
        Path targetFile = resolveUniqueTarget(targetDirectory, sourceFile.getFileName().toString());
        try {
            Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return targetFile;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to move file from "
                    + sourceFile.toAbsolutePath() + " to " + targetFile.toAbsolutePath(), ex);
        }
    }

    private Path resolveUniqueTarget(Path targetDirectory, String originalFileName) {
        int dotIndex = originalFileName.lastIndexOf('.');
        String namePart = dotIndex > 0 ? originalFileName.substring(0, dotIndex) : originalFileName;
        String extPart = dotIndex > 0 ? originalFileName.substring(dotIndex) : "";

        LocalDateTime now = now();
        String baseWithDateTs = namePart + "_" + now.format(FILE_DATE) + "_" + now.format(FILE_TS);
        Path candidate = targetDirectory.resolve(baseWithDateTs + extPart);
        int counter = 1;
        while (Files.exists(candidate)) {
            candidate = targetDirectory.resolve(baseWithDateTs + "_" + counter + extPart);
            counter++;
        }
        return candidate;
    }

    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
