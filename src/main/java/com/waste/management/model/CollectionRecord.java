package com.waste.management.model;

import java.time.LocalDateTime;

public class CollectionRecord {
    private Long id;
    private Long taskId;
    private String taskCode;
    private Long wasteBinId;
    private String binCode;
    private String locationName;
    private LocalDateTime collectedAt;
    private double fillPercentageBefore;
    private double fillPercentageAfter;
    private double weightCollectedKg;
    private String remarks;

    public CollectionRecord() {
        this.fillPercentageAfter = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public double getFillPercentageBefore() {
        return fillPercentageBefore;
    }

    public void setFillPercentageBefore(double fillPercentageBefore) {
        this.fillPercentageBefore = fillPercentageBefore;
    }

    public double getFillPercentageAfter() {
        return fillPercentageAfter;
    }

    public void setFillPercentageAfter(double fillPercentageAfter) {
        this.fillPercentageAfter = fillPercentageAfter;
    }

    public double getWeightCollectedKg() {
        return weightCollectedKg;
    }

    public void setWeightCollectedKg(double weightCollectedKg) {
        this.weightCollectedKg = weightCollectedKg;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
