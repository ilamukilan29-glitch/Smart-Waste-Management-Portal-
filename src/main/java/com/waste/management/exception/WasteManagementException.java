package com.waste.management.exception;

public class WasteManagementException extends RuntimeException {
    public WasteManagementException(String message) {
        super(message);
    }
    public WasteManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
