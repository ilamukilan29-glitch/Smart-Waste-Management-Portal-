package com.waste.management.dto;

import com.waste.management.model.PriorityLevel;

public class RouteStopDTO {
    private int stopOrder;
    private String stopType; // DEPOT_START, BIN, DEPOT_END
    private Long wasteBinId;
    private String binCode;
    private String locationLabel;
    private String address;
    private double latitude;
    private double longitude;
    private double fillPercentage;
    private PriorityLevel priorityLevel;
    private double expectedWasteKg;
    private double cumulativeLoadKg;
    private double distanceFromPrevKm;
    private double cumulativeDistanceKm;
    private int estimatedLegMinutes;
    private String status = "PLANNED";

    public RouteStopDTO() {}

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        this.stopOrder = stopOrder;
    }

    public String getStopType() {
        return stopType;
    }

    public void setStopType(String stopType) {
        this.stopType = stopType;
    }

    public Long getWasteBinId() {
        return wasteBinId;
    }

    public void setWasteBinId(Long wasteBinId) {
        this.wasteBinId = wasteBinId;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
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

    public double getFillPercentage() {
        return fillPercentage;
    }

    public void setFillPercentage(double fillPercentage) {
        this.fillPercentage = fillPercentage;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public double getExpectedWasteKg() {
        return expectedWasteKg;
    }

    public void setExpectedWasteKg(double expectedWasteKg) {
        this.expectedWasteKg = expectedWasteKg;
    }

    public double getCumulativeLoadKg() {
        return cumulativeLoadKg;
    }

    public void setCumulativeLoadKg(double cumulativeLoadKg) {
        this.cumulativeLoadKg = cumulativeLoadKg;
    }

    public double getDistanceFromPrevKm() {
        return distanceFromPrevKm;
    }

    public void setDistanceFromPrevKm(double distanceFromPrevKm) {
        this.distanceFromPrevKm = distanceFromPrevKm;
    }

    public double getCumulativeDistanceKm() {
        return cumulativeDistanceKm;
    }

    public void setCumulativeDistanceKm(double cumulativeDistanceKm) {
        this.cumulativeDistanceKm = cumulativeDistanceKm;
    }

    public int getEstimatedLegMinutes() {
        return estimatedLegMinutes;
    }

    public void setEstimatedLegMinutes(int estimatedLegMinutes) {
        this.estimatedLegMinutes = estimatedLegMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
