package com.globaltrade.scm.api;

public class BusinessFault extends Exception {
    private final int status;
    private final String code;

    public BusinessFault(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    public int getStatus() { return status; }
    public String getCode() { return code; }
}
