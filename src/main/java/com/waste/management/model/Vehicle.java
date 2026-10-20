package com.waste.management.model;

import java.time.LocalDateTime;

public class Vehicle {
    private Long id;
    private String registrationNumber;
    private String vehicleType; // COMPACTOR_TRUCK, TIPPER_TRUCK, ELECTRIC_VAN, MINI_TRUCK
    private double capacityKg;
    private Long areaId;
    private String areaName;
    private Long depotId;
    private String depotName;
    private String status; // ACTIVE, MAINTENANCE, RETIRED
    private String availabilityStatus; // AVAILABLE, ON_ROUTE, UNAVAILABLE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Vehicle() {
        this.status = "ACTIVE";
        this.availabilityStatus = "AVAILABLE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getCapacityKg() {
        return capacityKg;
    }

    public void setCapacityKg(double capacityKg) {
        this.capacityKg = capacityKg;
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

    public Long getDepotId() {
        return depotId;
    }

    public void setDepotId(Long depotId) {
        this.depotId = depotId;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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

    public boolean isAvailable() {
        return "ACTIVE".equalsIgnoreCase(this.status) && "AVAILABLE".equalsIgnoreCase(this.availabilityStatus);
    }
}
