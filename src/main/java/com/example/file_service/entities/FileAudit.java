package com.example.file_service.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_audit")
@Data
public class FileAudit {
    @Id
    @GeneratedValue()
    @UuidGenerator
    private String fileAuditId;
    @Column(nullable = false)
    private String userType;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String fileId;
    @Column(nullable = false)
    private String action;
    private String userName;
    private String fileName;
    private String fileType;
    private String fileSize;

    @Column(nullable = false)
    private LocalDateTime actionTimeStamp;
}
