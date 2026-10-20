package com.waste.management.dto;

public class WorkerPerformanceDTO {
    private Long workerId;
    private String workerCode;
    private String fullName;
    private String phone;
    private String areaName;
    private long totalAssignedTasks;
    private long completedTasks;
    private long pendingTasks;
    private double totalCollectedKg;
    private double completionRate;

    public WorkerPerformanceDTO() {}

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerCode() {
        return workerCode;
    }

    public void setWorkerCode(String workerCode) {
        this.workerCode = workerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public long getTotalAssignedTasks() {
        return totalAssignedTasks;
    }

    public void setTotalAssignedTasks(long totalAssignedTasks) {
        this.totalAssignedTasks = totalAssignedTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public double getTotalCollectedKg() {
        return totalCollectedKg;
    }

    public void setTotalCollectedKg(double totalCollectedKg) {
        this.totalCollectedKg = totalCollectedKg;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }
}
