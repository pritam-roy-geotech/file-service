package com.example.file_service.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum AllowList {

    PNG("image/png"),
    JPEG("image/jpeg"),
    PDF("application/pdf"),
    XML("application/xml");

    private final String value;

    AllowList(String value){
        this.value = value;
    }
}
