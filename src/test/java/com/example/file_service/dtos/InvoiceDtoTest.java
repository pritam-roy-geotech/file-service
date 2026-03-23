package com.example.file_service.dtos;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceDtoTest {

    @Test
    void process_ValidItems_ShouldCalculateTotals() {
        // Arrange
        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setItems(List.of(
            new InvoiceDto.Item() {{
                setItemPrice(10.0);
                setItemQuantity(2);
            }},
            new InvoiceDto.Item() {{
                setItemPrice(5.0);
                setItemQuantity(3);
            }}
        ));

        // Act
        invoiceDto.process();

        // Assert
        assertEquals(35.0, invoiceDto.getTotalAmount());
        assertEquals(5, invoiceDto.getTotalQuantity());
    }

    @Test
    void process_EmptyItems_ShouldSetZeroTotals() {
        // Arrange
        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setItems(List.of());

        // Act
        invoiceDto.process();

        // Assert
        assertEquals(0.0, invoiceDto.getTotalAmount());
        assertEquals(0, invoiceDto.getTotalQuantity());
    }

    @Test
    void generateInvoiceId_ShouldGenerateUniqueId() {
        // Arrange
        InvoiceDto invoiceDto1 = new InvoiceDto();
        InvoiceDto invoiceDto2 = new InvoiceDto();

        // Act
        invoiceDto1.generateInvoiceId();
        invoiceDto2.generateInvoiceId();

        // Assert
        assertNotNull(invoiceDto1.getInvoiceId());
        assertNotNull(invoiceDto2.getInvoiceId());
        assertNotEquals(invoiceDto1.getInvoiceId(), invoiceDto2.getInvoiceId());
    }
}
