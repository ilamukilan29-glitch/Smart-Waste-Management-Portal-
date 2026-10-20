package com.waste.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CollectionTask {
    private Long id;
    private String taskCode;
    private Long collectionPlanId;
    private String planCode;
    private Long workerId;
    private String workerName;
    private Long vehicleId;
    private String vehicleRegistration;
    private LocalDate plannedDate;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualCompletedAt;
    private double collectedWeightKg;
    private String remarks;
    private TaskStatus status; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CollectionTask() {
        this.status = TaskStatus.ASSIGNED;
        this.collectedWeightKg = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Long getCollectionPlanId() {
        return collectionPlanId;
    }

    public void setCollectionPlanId(Long collectionPlanId) {
        this.collectionPlanId = collectionPlanId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public LocalDateTime getActualStartAt() {
        return actualStartAt;
    }

    public void setActualStartAt(LocalDateTime actualStartAt) {
        this.actualStartAt = actualStartAt;
    }

    public LocalDateTime getActualCompletedAt() {
        return actualCompletedAt;
    }

    public void setActualCompletedAt(LocalDateTime actualCompletedAt) {
        this.actualCompletedAt = actualCompletedAt;
    }

    public double getCollectedWeightKg() {
        return collectedWeightKg;
    }

    public void setCollectedWeightKg(double collectedWeightKg) {
        this.collectedWeightKg = collectedWeightKg;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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
}
