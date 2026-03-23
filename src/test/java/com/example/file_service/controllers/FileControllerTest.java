package com.example.file_service.controllers;

import com.example.file_service.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileController fileController;

    @Test
    void upload_ValidFile_ShouldReturnCreatedResponse() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        var storedFile = new FileStorageService.StoredFile("key123", "test.pdf", "application/pdf", 7L, Path.of("dummy"));
        when(fileStorageService.store(any(MultipartFile.class))).thenReturn(storedFile);

        // Act
        var response = fileController.upload(file);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("key123", body.get("key"));
        assertEquals("test.pdf", body.get("originalName"));
        assertEquals(7L, body.get("size"));
        assertEquals("application/pdf", body.get("contentType"));
    }

    @Test
    void upload_FileTooLarge_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[26 * 1024 * 1024]; // 26MB
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileController.upload(file));
    }

    @Test
    void upload_InvalidContentType_ShouldThrowException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileController.upload(file));
    }

    @Test
    void uploadBatch_ValidFiles_ShouldReturnList() throws Exception {
        // Arrange
        List<MultipartFile> files = List.of(
            new MockMultipartFile("files", "test1.pdf", "application/pdf", "content1".getBytes()),
            new MockMultipartFile("files", "test2.pdf", "application/pdf", "content2".getBytes())
        );
        when(fileStorageService.store(any(MultipartFile.class)))
            .thenReturn(new FileStorageService.StoredFile("key1", "test1.pdf", "application/pdf", 8L, Path.of("dummy1")))
            .thenReturn(new FileStorageService.StoredFile("key2", "test2.pdf", "application/pdf", 8L, Path.of("dummy2")));

        // Act
        var result = fileController.uploadBatch(files);

        // Assert
        assertEquals(2, result.size());
        assertEquals("key1", result.get(0).get("key"));
        assertEquals("key2", result.get(1).get("key"));
    }

    @Test
    void list_ShouldReturnFileList() throws Exception {
        // Arrange
        Path tempDir = Files.createTempDirectory("test-files");
        try {
            // Create some test files
            Files.createFile(tempDir.resolve("file1.pdf"));
            Files.createFile(tempDir.resolve("file2.txt"));
            // Create a directory to ensure it's filtered out
            Files.createDirectory(tempDir.resolve("subdir"));

            when(fileStorageService.resolve(".")).thenReturn(tempDir);

            // Act
            var result = fileController.list();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("file1.pdf"));
            assertTrue(result.contains("file2.txt"));
        } finally {
            // Clean up
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception e) {
                        // Ignore
                    }
                });
            }
        }
    }

    @Test
    void delete_ValidKey_ShouldReturnNoContent() throws Exception {
        // Arrange
        String key = "testkey";

        // Act
        var response = fileController.delete(key);

        // Assert
        assertEquals(204, response.getStatusCode().value());
        verify(fileStorageService).delete(key);
    }
}
