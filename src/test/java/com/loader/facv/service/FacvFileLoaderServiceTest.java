package com.loader.facv.service;

import com.loader.facv.model.FacvRecord;
import com.loader.facv.repository.FacvRecordRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FacvFileLoaderServiceTest {

    @Test
    void shouldReturnZeroWhenLoaderIsDisabled(@TempDir Path tempDir) {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, false, Arrays.asList("CLFACV.txt"), 1000
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        verifyNoInteractions(parser);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnZeroWhenNoFileNamesConfigured(@TempDir Path tempDir) {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Collections.<String>emptyList(), 1000
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        verifyNoInteractions(parser);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnZeroWhenFileNamesIsNull(@TempDir Path tempDir) {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(tempDir, parser, repository, true, null, 1000);

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        verifyNoInteractions(parser);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldSkipMissingFileAndReturnZero(@TempDir Path tempDir) {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        when(parser.expectedColumnLengths()).thenReturn(Collections.singletonList("BNK_NO(3)"));
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        Assertions.assertTrue(Files.isDirectory(incomingDir(tempDir)));
        Assertions.assertTrue(Files.isDirectory(successDir(tempDir)));
        Assertions.assertTrue(Files.isDirectory(failedDir(tempDir)));
        verify(parser).expectedColumnLengths();
        verify(parser, never()).parse(anyString());
        verify(repository, never()).batchInsert(anyList());
    }

    @Test
    void shouldSkipPathWhenItIsNotARegularFile(@TempDir Path tempDir) throws IOException {
        Files.createDirectories(incomingDir(tempDir).resolve("CLFACV.txt"));

        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        when(parser.expectedColumnLengths()).thenReturn(Collections.singletonList("BNK_NO(3)"));
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        verify(parser).expectedColumnLengths();
        verify(parser, never()).parse(anyString());
        verify(repository, never()).batchInsert(anyList());
    }

    @Test
    void shouldLoadFileInBatchesAndMoveToSuccess(@TempDir Path tempDir) throws IOException {
        Path incomingFile = incomingDir(tempDir).resolve("CLFACV.txt");
        Files.createDirectories(incomingFile.getParent());
        Files.write(incomingFile, Arrays.asList("LINE-1", " ", "LINE-2", "LINE-3"), StandardCharsets.UTF_8);

        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        when(parser.expectedColumnLengths()).thenReturn(Collections.singletonList("BNK_NO(3)"));
        when(parser.parse(anyString())).thenReturn(record("1"), record("2"), record("3"));

        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        List<Integer> batchSizes = new ArrayList<Integer>();
        doAnswer(invocation -> {
            List<FacvRecord> rows = invocation.getArgument(0);
            batchSizes.add(rows.size());
            return null;
        }).when(repository).batchInsert(anyList());

        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 2
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(3L, inserted);
        Assertions.assertEquals(Arrays.asList(2, 1), batchSizes);
        Assertions.assertFalse(Files.exists(incomingFile));

        Assertions.assertEquals(1L, countFiles(successDir(tempDir)));
        Assertions.assertEquals(0L, countFiles(failedDir(tempDir)));

        String movedName = firstFileName(successDir(tempDir));
        Assertions.assertTrue(movedName.startsWith("CLFACV_"));
        Assertions.assertTrue(movedName.endsWith(".txt"));
    }

    @Test
    void shouldLoadFileWhenBatchEndsExactlyWithoutRemainder(@TempDir Path tempDir) throws IOException {
        Path incomingFile = incomingDir(tempDir).resolve("CLFACV.txt");
        Files.createDirectories(incomingFile.getParent());
        Files.write(incomingFile, Arrays.asList("LINE-1", "LINE-2"), StandardCharsets.UTF_8);

        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        when(parser.expectedColumnLengths()).thenReturn(Collections.singletonList("BNK_NO(3)"));
        when(parser.parse(anyString())).thenReturn(record("1"), record("2"));

        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        List<Integer> batchSizes = new ArrayList<Integer>();
        doAnswer(invocation -> {
            List<FacvRecord> rows = invocation.getArgument(0);
            batchSizes.add(rows.size());
            return null;
        }).when(repository).batchInsert(anyList());

        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 2
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(2L, inserted);
        Assertions.assertEquals(Arrays.asList(2), batchSizes);
        Assertions.assertEquals(1L, countFiles(successDir(tempDir)));
        Assertions.assertEquals(0L, countFiles(failedDir(tempDir)));
    }

    @Test
    void shouldMoveFileToFailedWhenParsingFails(@TempDir Path tempDir) throws IOException {
        Path incomingFile = incomingDir(tempDir).resolve("CLFACVHASE.txt");
        Files.createDirectories(incomingFile.getParent());
        Files.write(incomingFile, Arrays.asList("BAD-LINE"), StandardCharsets.UTF_8);

        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        when(parser.expectedColumnLengths()).thenReturn(Collections.singletonList("BNK_NO(3)"));
        when(parser.parse(anyString())).thenThrow(new IllegalArgumentException("parse failed"));

        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACVHASE.txt"), 1000
        );

        long inserted = service.loadAllConfiguredFiles();

        Assertions.assertEquals(0L, inserted);
        verify(repository, never()).batchInsert(anyList());
        Assertions.assertEquals(0L, countFiles(successDir(tempDir)));
        Assertions.assertEquals(1L, countFiles(failedDir(tempDir)));
        Assertions.assertFalse(Files.exists(incomingFile));
        String movedName = firstFileName(failedDir(tempDir));
        Assertions.assertTrue(movedName.startsWith("CLFACVHASE_"));
        Assertions.assertTrue(movedName.endsWith(".txt"));
    }

    @Test
    void shouldThrowWhenDirectoryPathPointsToAFile(@TempDir Path tempDir) throws IOException {
        Path incomingAsFile = tempDir.resolve("incoming-as-file");
        Files.write(incomingAsFile, Arrays.asList("X"), StandardCharsets.UTF_8);

        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                parser,
                repository,
                true,
                incomingAsFile.toString(),
                successDir(tempDir).toString(),
                failedDir(tempDir).toString(),
                Arrays.asList("CLFACV.txt"),
                "UTF-8",
                1000
        );

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, service::loadAllConfiguredFiles);
        Assertions.assertTrue(ex.getMessage().contains("Failed to create/access directory"));
    }

    @Test
    void shouldHandleMissingSourceInMoveToFailedSafely(@TempDir Path tempDir) throws Exception {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        Path missingSource = tempDir.resolve("missing.txt");
        Path failedDir = failedDir(tempDir);
        Files.createDirectories(failedDir);

        Method method = FacvFileLoaderService.class.getDeclaredMethod("moveToFailedSafely", Path.class, Path.class);
        method.setAccessible(true);
        method.invoke(service, missingSource, failedDir);

        Assertions.assertEquals(0L, countFiles(failedDir));
    }

    @Test
    void shouldHandleMoveFailureInsideMoveToFailedSafely(@TempDir Path tempDir) throws Exception {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        Path source = tempDir.resolve("CLFACV.txt");
        Files.write(source, Arrays.asList("X"), StandardCharsets.UTF_8);

        Path failedAsFile = tempDir.resolve("failed-as-file");
        Files.write(failedAsFile, Arrays.asList("Y"), StandardCharsets.UTF_8);

        Method method = FacvFileLoaderService.class.getDeclaredMethod("moveToFailedSafely", Path.class, Path.class);
        method.setAccessible(true);
        method.invoke(service, source, failedAsFile);

        Assertions.assertTrue(Files.exists(source));
    }

    @Test
    void shouldWrapIOExceptionFromLoadSingleFile(@TempDir Path tempDir) throws Exception {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        Method method = FacvFileLoaderService.class.getDeclaredMethod("loadSingleFile", Path.class);
        method.setAccessible(true);

        InvocationTargetException ex = Assertions.assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(service, tempDir.resolve("does-not-exist.txt"))
        );
        Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
        Assertions.assertTrue(ex.getCause().getMessage().contains("Failed to read file"));
    }

    @Test
    void shouldWrapIOExceptionFromMoveFile(@TempDir Path tempDir) throws Exception {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        FacvFileLoaderService service = newService(
                tempDir, parser, repository, true, Arrays.asList("CLFACV.txt"), 1000
        );

        Method method = FacvFileLoaderService.class.getDeclaredMethod("moveFile", Path.class, Path.class);
        method.setAccessible(true);
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);

        InvocationTargetException ex = Assertions.assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(service, tempDir.resolve("missing-source.txt"), targetDir)
        );
        Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
        Assertions.assertTrue(ex.getCause().getMessage().contains("Failed to move file"));
    }

    @Test
    void shouldResolveUniqueNameWithCounterAndWithoutExtension(@TempDir Path tempDir) throws Exception {
        FixedWidthFacvParser parser = mock(FixedWidthFacvParser.class);
        FacvRecordRepository repository = mock(FacvRecordRepository.class);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 3, 9, 10, 11, 12, 123_000_000);
        FacvFileLoaderService service = new FixedNowFacvFileLoaderService(parser, repository, fixedNow);
        configureService(
                service,
                true,
                incomingDir(tempDir).toString(),
                successDir(tempDir).toString(),
                failedDir(tempDir).toString(),
                Arrays.asList("CLFACV.txt"),
                "UTF-8",
                1000
        );

        Path targetDir = tempDir.resolve("success");
        Files.createDirectories(targetDir);
        Path firstName = targetDir.resolve("CLFACV_09032026_101112123");
        Files.write(firstName, Arrays.asList("already exists"), StandardCharsets.UTF_8);

        Method method = FacvFileLoaderService.class.getDeclaredMethod("resolveUniqueTarget", Path.class, String.class);
        method.setAccessible(true);
        Path resolved = (Path) method.invoke(service, targetDir, "CLFACV");

        Assertions.assertEquals("CLFACV_09032026_101112123_1", resolved.getFileName().toString());
    }

    private static FacvFileLoaderService newService(
            Path root,
            FixedWidthFacvParser parser,
            FacvRecordRepository repository,
            boolean enabled,
            List<String> fileNames,
            int batchSize
    ) {
        return newService(
                parser,
                repository,
                enabled,
                incomingDir(root).toString(),
                successDir(root).toString(),
                failedDir(root).toString(),
                fileNames,
                "UTF-8",
                batchSize
        );
    }

    private static FacvFileLoaderService newService(
            FixedWidthFacvParser parser,
            FacvRecordRepository repository,
            boolean enabled,
            String incomingPath,
            String successPath,
            String failedPath,
            List<String> fileNames,
            String charset,
            int batchSize
    ) {
        FacvFileLoaderService service = new FacvFileLoaderService(parser, repository);
        configureService(service, enabled, incomingPath, successPath, failedPath, fileNames, charset, batchSize);
        return service;
    }

    private static void configureService(
            FacvFileLoaderService service,
            boolean enabled,
            String incomingPath,
            String successPath,
            String failedPath,
            List<String> fileNames,
            String charset,
            int batchSize
    ) {
        ReflectionTestUtils.setField(service, "enabled", enabled);
        ReflectionTestUtils.setField(service, "incomingPath", incomingPath);
        ReflectionTestUtils.setField(service, "successPath", successPath);
        ReflectionTestUtils.setField(service, "failedPath", failedPath);
        ReflectionTestUtils.setField(service, "fileNames", fileNames);
        ReflectionTestUtils.setField(service, "charset", charset);
        ReflectionTestUtils.setField(service, "batchSize", batchSize);
    }

    private static Path incomingDir(Path root) {
        return root.resolve("incoming");
    }

    private static Path successDir(Path root) {
        return root.resolve("success");
    }

    private static Path failedDir(Path root) {
        return root.resolve("failed");
    }

    private static FacvRecord record(String key) {
        FacvRecord record = new FacvRecord();
        record.setBnkNo("00" + key);
        return record;
    }

    private static long countFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.count();
        }
    }

    private static String firstFileName(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            Path path = stream.findFirst().orElseThrow(AssertionError::new);
            return path.getFileName().toString();
        }
    }

    private static final class FixedNowFacvFileLoaderService extends FacvFileLoaderService {

        private final LocalDateTime fixedNow;

        private FixedNowFacvFileLoaderService(
                FixedWidthFacvParser parser,
                FacvRecordRepository repository,
                LocalDateTime fixedNow
        ) {
            super(parser, repository);
            this.fixedNow = fixedNow;
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }
    }
}
