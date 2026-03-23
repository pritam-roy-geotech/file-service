package com.example.file_service.services;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {


    @TempDir
    Path tempDir;

    @Mock
    FileAuditService fileAuditService;

   FileStorageService fileStorageService;

    @BeforeEach
    void init(){
        fileStorageService = new FileStorageService(fileAuditService);
        // inject @Value field manually
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());

        // manually call @PostConstruct
        fileStorageService.init();
    }


    @Test
    void store_ValidFile_ShouldStoreSuccessfully() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
//        when(mockFile.getSize()).thenReturn(12L);

        // Act
        var result = fileStorageService.store(mockFile);

        // Assert
        assertNotNull(result.key());
        assertEquals("test.pdf", result.originalName());
        assertEquals("application/pdf", result.contentType());
        assertEquals(12L, result.size());
        assertTrue(Files.exists(result.path()));
    }

    @Test
    void store_EmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(mockFile));
    }

    @Test
    void store_InvalidPath_ShouldThrowException() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("../test.pdf");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(mockFile));
    }

    @Test
    void load_ExistingFile_ShouldReturnStoredFile() throws Exception {
        // Arrange
        String key = "testkey.pdf";
        Path filePath = tempDir.resolve(key);
        Files.write(filePath, "test content".getBytes());

        // Act
        var result = fileStorageService.load(key);

        // Assert
        assertEquals(key, result.key());
        assertEquals(key, result.originalName());
        assertNotNull(result.contentType());
        assertEquals(12L, result.size());
    }

    @Test
    void load_NonExistingFile_ShouldThrowException() {
        // Act & Assert
        assertThrows(java.nio.file.NoSuchFileException.class, () -> fileStorageService.load("nonexistent"));
    }

    @Test
    void delete_ExistingFile_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        String key = "testkey.pdf";
        Path filePath = tempDir.resolve(key);
        Files.write(filePath, "test content".getBytes());

        // Act
        fileStorageService.delete(key);

        // Assert
        assertFalse(Files.exists(filePath));
    }

    @Test
    void resolve_ValidKey_ShouldReturnPath() {
        // Act
        Path result = fileStorageService.resolve("testkey");

        // Assert
        assertEquals(tempDir.resolve("testkey"), result);
    }

    @Test
    void resolve_PathTraversal_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.resolve("../outside"));
    }
}
