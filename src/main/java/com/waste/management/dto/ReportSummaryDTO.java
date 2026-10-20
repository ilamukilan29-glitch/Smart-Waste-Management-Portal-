package com.waste.management.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportSummaryDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long areaFilterId;
    private String areaFilterName;

    private long totalCollections;
    private long completedCollections;
    private long pendingCollections;
    private double totalWasteCollectedKg;
    private double totalDistanceTraveledKm;

    private Map<String, Double> wasteCollectedByType = new HashMap<>();
    private Map<String, Long> collectionsByArea = new HashMap<>();
    private List<WorkerPerformanceDTO> workerPerformances = new ArrayList<>();

    public ReportSummaryDTO() {}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getAreaFilterId() {
        return areaFilterId;
    }

    public void setAreaFilterId(Long areaFilterId) {
        this.areaFilterId = areaFilterId;
    }

    public String getAreaFilterName() {
        return areaFilterName;
    }

    public void setAreaFilterName(String areaFilterName) {
        this.areaFilterName = areaFilterName;
    }

    public long getTotalCollections() {
        return totalCollections;
    }

    public void setTotalCollections(long totalCollections) {
        this.totalCollections = totalCollections;
    }

    public long getCompletedCollections() {
        return completedCollections;
    }

    public void setCompletedCollections(long completedCollections) {
        this.completedCollections = completedCollections;
    }

    public long getPendingCollections() {
        return pendingCollections;
    }

    public void setPendingCollections(long pendingCollections) {
        this.pendingCollections = pendingCollections;
    }

    public double getTotalWasteCollectedKg() {
        return totalWasteCollectedKg;
    }

    public void setTotalWasteCollectedKg(double totalWasteCollectedKg) {
        this.totalWasteCollectedKg = totalWasteCollectedKg;
    }

    public double getTotalDistanceTraveledKm() {
        return totalDistanceTraveledKm;
    }

    public void setTotalDistanceTraveledKm(double totalDistanceTraveledKm) {
        this.totalDistanceTraveledKm = totalDistanceTraveledKm;
    }

    public Map<String, Double> getWasteCollectedByType() {
        return wasteCollectedByType;
    }

    public void setWasteCollectedByType(Map<String, Double> wasteCollectedByType) {
        this.wasteCollectedByType = wasteCollectedByType;
    }

    public Map<String, Long> getCollectionsByArea() {
        return collectionsByArea;
    }

    public void setCollectionsByArea(Map<String, Long> collectionsByArea) {
        this.collectionsByArea = collectionsByArea;
    }

    public List<WorkerPerformanceDTO> getWorkerPerformances() {
        return workerPerformances;
    }

    public void setWorkerPerformances(List<WorkerPerformanceDTO> workerPerformances) {
        this.workerPerformances = workerPerformances;
    }
}
