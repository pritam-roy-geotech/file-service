package com.example.file_service.services;

import com.example.file_service.dtos.InvoiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.rmi.server.ExportException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FileGeneratorServices {

    private final TemplateEngine templateEngine;

    public ByteArrayOutputStream generatePdf(InvoiceDto dto) throws Exception {

        dto.process();
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("invoice", dto);
        String html = templateEngine.process("invoice.html", ctx);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();

        return baos;
    }
}
