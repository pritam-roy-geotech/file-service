package com.example.file_service.services;


import com.example.file_service.enums.FileAction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.mock.web.MockMultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir}")         // ← inject as field
    private String uploadDir;

    private  Path root;

    private final FileAuditService fileAuditService;

    @PostConstruct                        // ← no parameters
    public void init() {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root); // ← create folder if not exists
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public StoredFile store(MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");

        // sanitize filename
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (originalName.contains("..")) throw new IllegalArgumentException("Invalid path sequence in filename");

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot);

        String key = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(key).normalize();

        // copy stream to disk (overwrite=false)
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        //auditing
        fileAuditService.saveFileAction(FileAction.UPLOAD,file,key);

        String contentType = file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        long size = Files.size(target);



        return new StoredFile(key, originalName, contentType, size, target);
    }

    public Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Path traversal");
        return path;
    }

    public StoredFile load(String key) throws Exception {
        Path p = resolve(key);
        if (!Files.exists(p)) throw new java.nio.file.NoSuchFileException(key);
        String contentType = Files.probeContentType(p);
        long size = Files.size(p);
        StoredFile storedFile= new StoredFile(key, key, contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE, size, p);

        //Auditing
        MultipartFile multipartFile = getMultipartFileFromDisk(p.toString());
        fileAuditService.saveFileAction(FileAction.DOWNLOAD,multipartFile,key);

        return storedFile;
    }

    public void delete(String key) throws Exception {

        //auditing
        Path path = resolve(key);
        String filePath=resolve(key).toString();
        MultipartFile file = getMultipartFileFromDisk(filePath);
        fileAuditService.saveFileAction(FileAction.DELETE,file,key);

        Files.deleteIfExists(resolve(key));

    }

    public record StoredFile(String key, String originalName, String contentType, long size, Path path) {}

    public MultipartFile getMultipartFileFromDisk(@Value("${file.upload-dir}") String filePath) throws Exception {
        Path path = Path.of(filePath);
        String name = path.getFileName().toString();
        String contentType = Files.probeContentType(path);

        return new MockMultipartFile(
                "file",
                name,
                contentType,
                Files.readAllBytes(path)
        );
    }

}
