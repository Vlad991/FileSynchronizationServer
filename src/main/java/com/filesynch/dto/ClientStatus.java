package com.filesynch.dto;

public enum ClientStatus {
    CONNECTED("CONNECTED"),
    DISCONNECTED("DISCONNECTED");

    private String status;

    private ClientStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
