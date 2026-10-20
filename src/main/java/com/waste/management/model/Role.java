package com.waste.management.model;

public enum Role {
    ADMIN("Administrator"),
    WORKER("Collection Worker"),
    SUPERVISOR("Supervisor");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
