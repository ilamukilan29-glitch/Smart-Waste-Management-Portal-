package com.waste.management.model;

import java.time.LocalDateTime;

public class WasteBin {
    private Long id;
    private String binCode;
    private Long areaId;
    private String areaName;
    private String locationName;
    private String address;
    private double latitude;
    private double longitude;
    private WasteType wasteType;
    private double capacityKg;
    private double currentFillPercentage;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE
    private LocalDateTime lastCollectionAt;
    private LocalDateTime lastReportedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WasteBin() {
        this.status = "ACTIVE";
        this.currentFillPercentage = 0.0;
        this.wasteType = WasteType.GENERAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public WasteType getWasteType() {
        return wasteType;
    }

    public void setWasteType(WasteType wasteType) {
        this.wasteType = wasteType;
    }

    public double getCapacityKg() {
        return capacityKg;
    }

    public void setCapacityKg(double capacityKg) {
        this.capacityKg = capacityKg;
    }

    public double getCurrentFillPercentage() {
        return currentFillPercentage;
    }

    public void setCurrentFillPercentage(double currentFillPercentage) {
        this.currentFillPercentage = currentFillPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastCollectionAt() {
        return lastCollectionAt;
    }

    public void setLastCollectionAt(LocalDateTime lastCollectionAt) {
        this.lastCollectionAt = lastCollectionAt;
    }

    public LocalDateTime getLastReportedAt() {
        return lastReportedAt;
    }

    public void setLastReportedAt(LocalDateTime lastReportedAt) {
        this.lastReportedAt = lastReportedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    /**
     * Calculates the estimated waste quantity currently in the bin (in kg).
     */
    public double getEstimatedWasteKg() {
        return (this.currentFillPercentage / 100.0) * this.capacityKg;
    }

    /**
     * Derives PriorityLevel based on standard thresholds:
     * 90% - 100% : CRITICAL
     * 80% - 89.99%: HIGH
     * 60% - 79.99%: MEDIUM
     * Below 60%  : LOW
     */
    public PriorityLevel getPriorityLevel() {
        if (currentFillPercentage >= 90.0) {
            return PriorityLevel.CRITICAL;
        } else if (currentFillPercentage >= 80.0) {
            return PriorityLevel.HIGH;
        } else if (currentFillPercentage >= 60.0) {
            return PriorityLevel.MEDIUM;
        } else {
            return PriorityLevel.LOW;
        }
    }
}
