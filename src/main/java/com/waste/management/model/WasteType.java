package com.waste.management.model;

public enum WasteType {
    ORGANIC("Organic Waste", "badge-success"),
    RECYCLABLE("Recyclable Waste", "badge-primary"),
    PLASTIC("Plastic Waste", "badge-warning"),
    PAPER("Paper Waste", "badge-info"),
    GENERAL("General Municipal Waste", "badge-secondary");

    private final String displayName;
    private final String badgeClass;

    WasteType(String displayName, String badgeClass) {
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
