package com.filesynch.dto;

public enum FilePartStatus {
    NOT_SENT("NOT_SENT"),
    SENT_WITHOUT_NOTICE("SENT_WITHOUT_NOTICE"), // file came to receiver, but sender not notified
    SENT("SENT"); //

    private String status;

    private FilePartStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
