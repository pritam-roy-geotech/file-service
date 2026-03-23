package com.example.file_service.controllers;

import com.example.file_service.dtos.InvoiceDto;
import com.example.file_service.services.FileGeneratorServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.ByteArrayOutputStream;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/generate")
public class FileGeneratorController {

    private final FileGeneratorServices fileGeneratorServices;

    @GetMapping(value = "/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> invoicePdf(@RequestBody InvoiceDto invoiceDto) throws Exception {

        invoiceDto.generateInvoiceId();

        ByteArrayOutputStream baos = fileGeneratorServices.generatePdf(invoiceDto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + invoiceDto.getInvoiceId() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(baos.size())
                .body(baos.toByteArray());
    }

}
