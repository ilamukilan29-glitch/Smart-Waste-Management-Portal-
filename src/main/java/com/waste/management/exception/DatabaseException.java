package com.waste.management.exception;

public class DatabaseException extends WasteManagementException {
    public DatabaseException(String message) {
        super(message);
    }
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
