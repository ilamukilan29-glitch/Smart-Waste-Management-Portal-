package com.waste.management.model;

public enum PriorityLevel {
    CRITICAL("Critical", "bg-danger", 4),
    HIGH("High", "bg-warning text-dark", 3),
    MEDIUM("Medium", "bg-info text-dark", 2),
    LOW("Low", "bg-success", 1);

    private final String displayName;
    private final String badgeClass;
    private final int weight;

    PriorityLevel(String displayName, String badgeClass, int weight) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public int getWeight() {
        return weight;
    }
}
