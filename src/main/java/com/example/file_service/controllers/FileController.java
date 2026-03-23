package com.example.file_service.controllers;

import com.example.file_service.enums.AllowList;
import com.example.file_service.services.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/files")
@Validated
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    // Single upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @NotNull @RequestPart("file") MultipartFile file) throws Exception {

        validateFile(file);
        var saved = storage.store(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "key", saved.key(),
                "originalName", saved.originalName(),
                "size", saved.size(),
                "contentType", saved.contentType()
        ));
    }

    // Multiple upload
    @PostMapping(path = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, Object>> uploadBatch(@RequestPart("files") List<MultipartFile> files) throws Exception {
        return files.stream().peek(this::validateFile).map(f -> {
            try {
                var s = storage.store(f);
                Map<String,Object> m = new HashMap<>();
                m.put("key", s.key());
                m.put("originalName",s.originalName());
                m.put("size",s.size());
                m.put("contentType",s.contentType());
                return m;
            } catch (Exception e) {
                throw new RuntimeException("Failed to store: " + f.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());
    }

    // Streamed download
    @GetMapping("/{key}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String key) throws Exception {
        var file = storage.load(key);
        Path p = file.path();
        InputStream in = Files.newInputStream(p, StandardOpenOption.READ);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.originalName()).build().toString())
                .contentLength(file.size())
                .body(new InputStreamResource(in));
    }

    // List (simple)
    @GetMapping
    public List<String> list() throws Exception {
        try (Stream<Path> s = Files.list(storage.resolve("."))) {
            return s.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).toList();
        }
    }

    // Delete
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) throws Exception {
        storage.delete(key);
        return ResponseEntity.noContent().build();
    }

    private void validateFile(MultipartFile file) {
        // Example: size and type checks (do more stringent checks as needed)
        long maxBytes = 25 * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File too large (max 25MB)");
        }
        String type = file.getContentType();
//        // Example allow list
//        if (type == null || !(type.equals(AllowList.PNG.getValue()) || type.equals(AllowList.JPEG.getValue()) || type.equals(AllowList.PDF.getValue()) || type.equals(AllowList.XML.getValue()))) {
//            throw new IllegalArgumentException("Unsupported content type: " + type);
//        }
        AllowList[] allowList = AllowList.values();
        List<String> allowedTypes = Arrays.stream(allowList).map(AllowList::getValue).toList();
        if(type == null || !allowedTypes.contains(type)){
            throw new IllegalArgumentException("Unsupported content type: " + type);

        }
    }
}