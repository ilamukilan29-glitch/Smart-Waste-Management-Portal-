package com.waste.management.dto;

import com.waste.management.model.PriorityLevel;
import com.waste.management.model.WasteBin;
import com.waste.management.model.WasteType;

public class BinPriorityDTO {
    private Long binId;
    private String binCode;
    private Long areaId;
    private String areaName;
    private String locationName;
    private WasteType wasteType;
    private double capacityKg;
    private double currentFillPercentage;
    private PriorityLevel priorityLevel;
    private boolean hasActiveTask;
    private String priorityReason;

    public BinPriorityDTO() {}

    public static BinPriorityDTO fromEntity(WasteBin bin, boolean hasActiveTask, String reason) {
        BinPriorityDTO dto = new BinPriorityDTO();
        dto.setBinId(bin.getId());
        dto.setBinCode(bin.getBinCode());
        dto.setAreaId(bin.getAreaId());
        dto.setAreaName(bin.getAreaName());
        dto.setLocationName(bin.getLocationName());
        dto.setWasteType(bin.getWasteType());
        dto.setCapacityKg(bin.getCapacityKg());
        dto.setCurrentFillPercentage(bin.getCurrentFillPercentage());
        dto.setPriorityLevel(bin.getPriorityLevel());
        dto.setHasActiveTask(hasActiveTask);
        dto.setPriorityReason(reason);
        return dto;
    }

    public Long getBinId() {
        return binId;
    }

    public void setBinId(Long binId) {
        this.binId = binId;
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

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public boolean isHasActiveTask() {
        return hasActiveTask;
    }

    public void setHasActiveTask(boolean hasActiveTask) {
        this.hasActiveTask = hasActiveTask;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }
}
