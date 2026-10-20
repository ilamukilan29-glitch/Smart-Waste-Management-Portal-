package com.waste.management.model;

import java.time.LocalDateTime;

public class RouteStop {
    private Long id;
    private Long collectionPlanId;
    private int stopOrder;
    private String stopType; // DEPOT_START, BIN, DEPOT_END
    private Long wasteBinId;
    private String binCode;
    private String locationLabel;
    private double distanceFromPrevKm;
    private double cumulativeDistanceKm;
    private double expectedWasteKg;
    private String status; // PENDING, VISITED, SKIPPED
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    public RouteStop() {
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCollectionPlanId() {
        return collectionPlanId;
    }

    public void setCollectionPlanId(Long collectionPlanId) {
        this.collectionPlanId = collectionPlanId;
    }

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

    public double getExpectedWasteKg() {
        return expectedWasteKg;
    }

    public void setExpectedWasteKg(double expectedWasteKg) {
        this.expectedWasteKg = expectedWasteKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
}
