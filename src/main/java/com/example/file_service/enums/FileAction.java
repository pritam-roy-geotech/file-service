package com.example.file_service.enums;


public enum FileAction {
    UPLOAD("UPLOAD"),
    DOWNLOAD("DOWNLOAD"),
    DELETE("DELETE");

    private final String action;
    FileAction(String action){
        this.action=action;
    }
    public String getAction() {
        return action;
    }
}
