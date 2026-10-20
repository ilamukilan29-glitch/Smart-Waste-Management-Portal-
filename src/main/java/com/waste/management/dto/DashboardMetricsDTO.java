package com.waste.management.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object aggregating all live dashboard KPIs.
 * Produced asynchronously using Java 8 CompletableFuture.
 */
public class DashboardMetricsDTO {

    private long totalBins;
    private long binsRequiringCollection; // >= 80%
    private long criticalBins; // >= 90%
    private long pendingTasks;
    private long collectionsCompletedToday;
    private long availableWorkers;
    private long availableVehicles;
    private double totalDistancePlannedKm;
    private double completionPercentage;

    // Chart datasets
    private Map<String, Long> binsByWasteType = new HashMap<>();
    private Map<String, Long> binsByPriority = new HashMap<>();
    private Map<String, Long> tasksByStatus = new HashMap<>();

    public DashboardMetricsDTO() {}

    public long getTotalBins() {
        return totalBins;
    }

    public void setTotalBins(long totalBins) {
        this.totalBins = totalBins;
    }

    public long getBinsRequiringCollection() {
        return binsRequiringCollection;
    }

    public void setBinsRequiringCollection(long binsRequiringCollection) {
        this.binsRequiringCollection = binsRequiringCollection;
    }

    public long getCriticalBins() {
        return criticalBins;
    }

    public void setCriticalBins(long criticalBins) {
        this.criticalBins = criticalBins;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public long getCollectionsCompletedToday() {
        return collectionsCompletedToday;
    }

    public void setCollectionsCompletedToday(long collectionsCompletedToday) {
        this.collectionsCompletedToday = collectionsCompletedToday;
    }

    public long getAvailableWorkers() {
        return availableWorkers;
    }

    public void setAvailableWorkers(long availableWorkers) {
        this.availableWorkers = availableWorkers;
    }

    public long getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(long availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public double getTotalDistancePlannedKm() {
        return totalDistancePlannedKm;
    }

    public void setTotalDistancePlannedKm(double totalDistancePlannedKm) {
        this.totalDistancePlannedKm = totalDistancePlannedKm;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public Map<String, Long> getBinsByWasteType() {
        return binsByWasteType;
    }

    public void setBinsByWasteType(Map<String, Long> binsByWasteType) {
        this.binsByWasteType = binsByWasteType;
    }

    public Map<String, Long> getBinsByPriority() {
        return binsByPriority;
    }

    public void setBinsByPriority(Map<String, Long> binsByPriority) {
        this.binsByPriority = binsByPriority;
    }

    public Map<String, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public void setTasksByStatus(Map<String, Long> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }
}
