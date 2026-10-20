package com.waste.management.dto;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanResultDTO {

    private Long areaId;
    private String areaName;
    private Long depotId;
    private String depotName;
    private Long vehicleId;
    private String vehicleRegistration;
    private double vehicleCapacityKg;

    private List<RouteStopDTO> stops = new ArrayList<>();
    private List<Long> unvisitedBinIds = new ArrayList<>();

    private double totalDistanceKm;
    private int totalDurationMinutes;
    private double totalWasteEstimatedKg;
    private boolean capacityExceeded;
    private String algorithmNotes;

    public RoutePlanResultDTO() {
        this.algorithmNotes = "Generated via Nearest Neighbor Heuristic with Priority Weighting. Note: Nearest Neighbor is a greedy heuristic algorithm that solves the Capacitated Vehicle Routing Problem (CVRP) in O(n^2) polynomial time and does not guarantee a globally optimal TSP tour.";
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

    public double getVehicleCapacityKg() {
        return vehicleCapacityKg;
    }

    public void setVehicleCapacityKg(double vehicleCapacityKg) {
        this.vehicleCapacityKg = vehicleCapacityKg;
    }

    public List<RouteStopDTO> getStops() {
        return stops;
    }

    public void setStops(List<RouteStopDTO> stops) {
        this.stops = stops;
    }

    public List<Long> getUnvisitedBinIds() {
        return unvisitedBinIds;
    }

    public void setUnvisitedBinIds(List<Long> unvisitedBinIds) {
        this.unvisitedBinIds = unvisitedBinIds;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public double getTotalWasteEstimatedKg() {
        return totalWasteEstimatedKg;
    }

    public void setTotalWasteEstimatedKg(double totalWasteEstimatedKg) {
        this.totalWasteEstimatedKg = totalWasteEstimatedKg;
    }

    public boolean isCapacityExceeded() {
        return capacityExceeded;
    }

    public void setCapacityExceeded(boolean capacityExceeded) {
        this.capacityExceeded = capacityExceeded;
    }

    public String getAlgorithmNotes() {
        return algorithmNotes;
    }

    public void setAlgorithmNotes(String algorithmNotes) {
        this.algorithmNotes = algorithmNotes;
    }
}
