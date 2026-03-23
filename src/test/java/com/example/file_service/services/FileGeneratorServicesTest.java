package com.example.file_service.services;

import com.example.file_service.dtos.InvoiceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileGeneratorServicesTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private FileGeneratorServices fileGeneratorServices;

    @Test
    void generatePdf_ValidInvoiceDto_ShouldGeneratePdf() throws Exception {
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

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html><body>Test HTML</body></html>");

        // Act
        ByteArrayOutputStream result = fileGeneratorServices.generatePdf(invoiceDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(templateEngine).process(eq("invoice.html"), any(Context.class));
    }

    @Test
    void generatePdf_NullItems_ShouldHandleGracefully() throws Exception {
        // Arrange

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setFullName("John Doe");
        invoiceDto.setItems(null);

//        doNothing().when(invoiceDto).process();
//        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html><body>Test HTML</body></html>");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> fileGeneratorServices.generatePdf(invoiceDto));
    }
}
