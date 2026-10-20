package com.waste.management.service;

import com.waste.management.dao.VehicleDAO;
import com.waste.management.exception.ValidationException;
import com.waste.management.model.Vehicle;
import com.waste.management.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class VehicleService {

    private final VehicleDAO vehicleDAO;

    public VehicleService() {
        this(new VehicleDAO());
    }

    public VehicleService(VehicleDAO vehicleDAO) {
        this.vehicleDAO = vehicleDAO;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleDAO.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleDAO.findById(id);
    }

    public List<Vehicle> getAvailableVehicles(Long areaId) {
        return vehicleDAO.findAvailableVehicles(areaId);
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        validateVehicle(vehicle);
        return vehicleDAO.save(vehicle);
    }

    public boolean updateVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId() <= 0) {
            throw new ValidationException("Invalid vehicle ID for update.");
        }
        validateVehicle(vehicle);
        return vehicleDAO.update(vehicle);
    }

    public boolean updateAvailability(Long vehicleId, String availabilityStatus) {
        return vehicleDAO.updateAvailability(vehicleId, availabilityStatus);
    }

    private void validateVehicle(Vehicle vehicle) {
        ValidationUtil.requireNonEmpty(vehicle.getRegistrationNumber(), "Registration Number");
        ValidationUtil.requireNonEmpty(vehicle.getVehicleType(), "Vehicle Type");
        ValidationUtil.validatePositiveCapacity(vehicle.getCapacityKg());
        if (vehicle.getAreaId() == null || vehicle.getAreaId() <= 0) {
            throw new ValidationException("Area is required.");
        }
        if (vehicle.getDepotId() == null || vehicle.getDepotId() <= 0) {
            throw new ValidationException("Depot is required.");
        }
    }
}
