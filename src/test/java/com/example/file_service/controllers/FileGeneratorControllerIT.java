package com.example.file_service.controllers;


import com.example.file_service.dtos.InvoiceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileGeneratorControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void test_invoicePdfSuccess() throws Exception{

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setFullName("John Doe");
        invoiceDto.setFirstName("John");
        invoiceDto.setLastName("Doe");
        invoiceDto.setDOB("01-01-1990");
        invoiceDto.setAge("34");
        invoiceDto.setAadhaarNo("1234-5678-9012");
        invoiceDto.setVoterNo("AB1234567");
        InvoiceDto.Item item1 = new InvoiceDto.Item();
        item1.setCurrency("INR");
        item1.setItemPrice(100.0);
        item1.setItemName("black-socks");
        item1.setItemQuantity(2);
        InvoiceDto.Item item2 = new InvoiceDto.Item();
        item2.setCurrency("INR");
        item2.setItemPrice(1000.0);
        item2.setItemName("blue-T-shirt");
        item2.setItemQuantity(1);
        invoiceDto.setItems(java.util.List.of(item1, item2));

        ObjectMapper objectMapper = new ObjectMapper();
        String invoiceJson = objectMapper.writeValueAsString(invoiceDto);
        String json = mockMvc.perform(get("/api/v1/generate/pdf")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(invoiceJson))
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"invoice-")))
                        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
    }


}
