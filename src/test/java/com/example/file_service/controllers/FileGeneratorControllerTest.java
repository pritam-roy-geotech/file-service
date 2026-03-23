package com.example.file_service.controllers;

import com.example.file_service.dtos.InvoiceDto;
import com.example.file_service.services.FileGeneratorServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileGeneratorControllerTest {

    @Mock
    private FileGeneratorServices fileGeneratorServices;

    @InjectMocks
    private FileGeneratorController fileGeneratorController;

    @Test
    void invoicePdf_ValidInvoiceDto_ShouldReturnPdfResponse() throws Exception {
        // Arrange
        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setFullName("John Doe");
        invoiceDto.setItems(List.of(
            new InvoiceDto.Item() {{
                setItemName("Item1");
                setItemPrice(10.0);
                setItemQuantity(2);
                setCurrency("USD");
            }}
        ));

        ByteArrayOutputStream mockBaos = new ByteArrayOutputStream();
        mockBaos.write("PDF content".getBytes());
        when(fileGeneratorServices.generatePdf(any(InvoiceDto.class))).thenReturn(mockBaos);

        // Act
        var response = fileGeneratorController.invoicePdf(invoiceDto);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("attachment"));
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("invoice-"));
    }
}
