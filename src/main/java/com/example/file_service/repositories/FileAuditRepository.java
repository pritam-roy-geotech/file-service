package com.example.file_service.repositories;

import com.example.file_service.entities.FileAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAuditRepository extends JpaRepository<FileAudit, String> {
}
