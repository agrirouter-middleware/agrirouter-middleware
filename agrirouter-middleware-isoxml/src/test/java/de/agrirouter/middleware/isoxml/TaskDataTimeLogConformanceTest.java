package de.agrirouter.middleware.isoxml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Disabled("Only necessary for conformance testing, takes a LONG time.s")
class TaskDataTimeLogConformanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"5c0ad7b4-598f-45c3-a0c1-47e781a6c51f.zip",
            "ecdc65b8-0d93-4804-8234-5948624e2f52.zip",
            "1fe5b512-088b-45d8-848a-9cfc61ad19e4.zip",
            "b49660be-0749-4e1f-90cc-a3cc6800a305.zip",
            "4fc231c3-b989-4831-968b-fc86c9aeb744.zip",
            "186abefa-91f5-4f02-8927-62092bb5020c.zip"})
    void givenValidTaskDataFileWhenParsingTimeLogsFromBinariesThenTheDocumentsShouldBeParsedWithoutProblems(String fileName) throws IOException {
        final var taskDataTimeLogService = new TaskDataTimeLogService();
        final var zipFileAsBase64EncodedValue = getZipFileAsBase64EncodedValue(fileName);
        taskDataTimeLogService.parseMessageContent(zipFileAsBase64EncodedValue);
    }

    private byte[] getZipFileAsBase64EncodedValue(String fileName) throws IOException {
        final var workingDir = Path.of("", "src/test/resources");
        final var file = workingDir.resolve("examples/" + fileName);
        final var bytes = Files.readAllBytes(file);
        return Base64.getEncoder().encode(bytes);
    }


}