package com.example.file_service.services;

import com.example.file_service.entities.FileAudit;
import com.example.file_service.enums.FileAction;
import com.example.file_service.repositories.FileAuditRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class FileAuditService {

    private final FileAuditRepository fileAuditRepository;

    public boolean saveFileAction(FileAction fileAction, MultipartFile file, String fileKey){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = (String) jwt.getClaims().get("sub");
            String username = (String) jwt.getClaims().get("preferred_username");


            FileAudit fileAudit = new FileAudit();
            fileAudit.setAction(fileAction.getAction());
            fileAudit.setFileId(fileKey);
            fileAudit.setUserId(userId);
            String unSatinizedUserType = ((ArrayList<String>) jwt.getClaim("user-group")).get(0);
            fileAudit.setUserType(unSatinizedUserType.substring(1, unSatinizedUserType.length()));
            fileAudit.setFileName(fileKey);
            fileAudit.setFileSize(String.valueOf(file.getSize()));
            fileAudit.setFileType(file.getContentType());
            fileAudit.setUserName(username);
            fileAudit.setActionTimeStamp(LocalDateTime.now());

            fileAuditRepository.save(fileAudit);

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
