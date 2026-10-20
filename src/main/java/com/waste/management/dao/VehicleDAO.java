package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.Vehicle;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleDAO.class);

    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.id, v.registration_number, v.vehicle_type, v.capacity_kg, v.area_id, v.depot_id, " +
                     "v.status, v.availability_status, v.created_at, v.updated_at, a.name AS area_name, d.name AS depot_name " +
                     "FROM vehicles v " +
                     "JOIN areas a ON v.area_id = a.id " +
                     "JOIN depots d ON v.depot_id = d.id " +
                     "ORDER BY v.registration_number ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all vehicles", e);
            throw new DatabaseException("Failed to load vehicles", e);
        }
        return vehicles;
    }

    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT v.id, v.registration_number, v.vehicle_type, v.capacity_kg, v.area_id, v.depot_id, " +
                     "v.status, v.availability_status, v.created_at, v.updated_at, a.name AS area_name, d.name AS depot_name " +
                     "FROM vehicles v " +
                     "JOIN areas a ON v.area_id = a.id " +
                     "JOIN depots d ON v.depot_id = d.id " +
                     "WHERE v.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding vehicle by id: {}", id, e);
            throw new DatabaseException("Failed to find vehicle", e);
        }
        return Optional.empty();
    }

    public List<Vehicle> findAvailableVehicles(Long areaId) {
        List<Vehicle> vehicles = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT v.id, v.registration_number, v.vehicle_type, v.capacity_kg, v.area_id, v.depot_id, " +
            "v.status, v.availability_status, v.created_at, v.updated_at, a.name AS area_name, d.name AS depot_name " +
            "FROM vehicles v " +
            "JOIN areas a ON v.area_id = a.id " +
            "JOIN depots d ON v.depot_id = d.id " +
            "WHERE v.status = 'ACTIVE' AND v.availability_status = 'AVAILABLE' "
        );
        if (areaId != null && areaId > 0) {
            sql.append("AND v.area_id = ? ");
        }
        sql.append("ORDER BY v.capacity_kg DESC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (areaId != null && areaId > 0) {
                stmt.setLong(1, areaId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding available vehicles", e);
            throw new DatabaseException("Failed to find available vehicles", e);
        }
        return vehicles;
    }

    public Vehicle save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (registration_number, vehicle_type, capacity_kg, area_id, depot_id, " +
                     "status, availability_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehicle.getRegistrationNumber());
            stmt.setString(2, vehicle.getVehicleType());
            stmt.setDouble(3, vehicle.getCapacityKg());
            stmt.setLong(4, vehicle.getAreaId());
            stmt.setLong(5, vehicle.getDepotId());
            stmt.setString(6, vehicle.getStatus() != null ? vehicle.getStatus() : "ACTIVE");
            stmt.setString(7, vehicle.getAvailabilityStatus() != null ? vehicle.getAvailabilityStatus() : "AVAILABLE");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getLong(1));
                }
            }
            return vehicle;
        } catch (SQLException e) {
            LOGGER.error("Error saving vehicle: {}", vehicle.getRegistrationNumber(), e);
            throw new DatabaseException("Failed to save vehicle", e);
        }
    }

    public boolean update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET registration_number = ?, vehicle_type = ?, capacity_kg = ?, " +
                     "area_id = ?, depot_id = ?, status = ?, availability_status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getRegistrationNumber());
            stmt.setString(2, vehicle.getVehicleType());
            stmt.setDouble(3, vehicle.getCapacityKg());
            stmt.setLong(4, vehicle.getAreaId());
            stmt.setLong(5, vehicle.getDepotId());
            stmt.setString(6, vehicle.getStatus());
            stmt.setString(7, vehicle.getAvailabilityStatus());
            stmt.setLong(8, vehicle.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating vehicle: {}", vehicle.getId(), e);
            throw new DatabaseException("Failed to update vehicle", e);
        }
    }

    public boolean updateAvailability(Long vehicleId, String availabilityStatus) {
        String sql = "UPDATE vehicles SET availability_status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, availabilityStatus);
            stmt.setLong(2, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating vehicle availability: {}", vehicleId, e);
            throw new DatabaseException("Failed to update vehicle availability", e);
        }
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getLong("id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setCapacityKg(rs.getDouble("capacity_kg"));
        v.setAreaId(rs.getLong("area_id"));
        v.setAreaName(rs.getString("area_name"));
        v.setDepotId(rs.getLong("depot_id"));
        v.setDepotName(rs.getString("depot_name"));
        v.setStatus(rs.getString("status"));
        v.setAvailabilityStatus(rs.getString("availability_status"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            v.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            v.setUpdatedAt(updated.toLocalDateTime());
        }
        return v;
    }
}
