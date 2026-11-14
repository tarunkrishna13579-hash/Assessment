package com.azentio.sleeper.dto;

public class CoreEventResponse {

    private String message;

    private boolean duplicate;

    public CoreEventResponse(String message, boolean duplicate) {
        this.message = message;
        this.duplicate = duplicate;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDuplicate() {
        return duplicate;
    }
}