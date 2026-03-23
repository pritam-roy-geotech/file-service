package com.example.file_service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class InvoiceDto {

    @JsonIgnore
    String invoiceId;
    String fullName;
    String firstName;
    String lastName;
    String age;
    String DOB;
    String aadhaarNo;
    String voterNo;
    @JsonIgnore
    String BillGenerationDate = LocalDateTime.now().toString();
    @JsonIgnore
    Double totalAmount;
    @JsonIgnore
    int totalQuantity;
    List<Item> items;

    public void process() {
        double totalAmount = 0.0;
        int totalQuantity = 0;
        for (Item item : items) {
            totalAmount = totalAmount + (item.itemPrice * item.itemQuantity);
            totalQuantity = totalQuantity + item.itemQuantity;
        }
        this.totalAmount = totalAmount;
        this.totalQuantity = totalQuantity;
    }

    public void generateInvoiceId() {
        this.invoiceId = UUID.randomUUID().toString();
    }

    @Data
    public static class Item {
        Double itemPrice;
        Integer itemQuantity;
        String itemName;
        String currency;
    }


}
