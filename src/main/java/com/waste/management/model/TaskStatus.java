package com.waste.management.model;

public enum TaskStatus {
    DRAFT("Draft", "bg-secondary"),
    PENDING("Pending", "bg-secondary"),
    ASSIGNED("Assigned", "bg-primary"),
    IN_PROGRESS("In Progress", "bg-warning text-dark"),
    COMPLETED("Completed", "bg-success"),
    CANCELLED("Cancelled", "bg-dark");

    private final String displayName;
    private final String badgeClass;

    TaskStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
