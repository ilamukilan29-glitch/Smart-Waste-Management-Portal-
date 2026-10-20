package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.LocationDistance;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationDistanceDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDistanceDAO.class);

    /**
     * Looks up distance between two points (symmetric check: from->to or to->from).
     */
    public Optional<Double> getDistance(String fromType, Long fromId, String toType, Long toId) {
        if (fromType.equalsIgnoreCase(toType) && fromId.equals(toId)) {
            return Optional.of(0.0);
        }

        String sql = "SELECT distance_km FROM location_distances " +
                     "WHERE (from_type = ? AND from_id = ? AND to_type = ? AND to_id = ?) " +
                     "   OR (from_type = ? AND from_id = ? AND to_type = ? AND to_id = ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fromType);
            stmt.setLong(2, fromId);
            stmt.setString(3, toType);
            stmt.setLong(4, toId);

            stmt.setString(5, toType);
            stmt.setLong(6, toId);
            stmt.setString(7, fromType);
            stmt.setLong(8, fromId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getDouble("distance_km"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error looking up distance between {}:{} and {}:{}", fromType, fromId, toType, toId, e);
            throw new DatabaseException("Failed to lookup route distance", e);
        }
        return Optional.empty();
    }

    public List<LocationDistance> getAllDistances() {
        List<LocationDistance> list = new ArrayList<>();
        String sql = "SELECT id, from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes FROM location_distances";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocationDistance d = new LocationDistance();
                d.setId(rs.getLong("id"));
                d.setFromType(rs.getString("from_type"));
                d.setFromId(rs.getLong("from_id"));
                d.setToType(rs.getString("to_type"));
                d.setToId(rs.getLong("to_id"));
                d.setDistanceKm(rs.getDouble("distance_km"));
                d.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
                list.add(d);
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing location distances", e);
            throw new DatabaseException("Failed to load distance matrix", e);
        }
        return list;
    }

    public boolean saveOrUpdate(LocationDistance dist) {
        String sql = "INSERT INTO location_distances (from_type, from_id, to_type, to_id, distance_km, estimated_time_minutes) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE distance_km = VALUES(distance_km), estimated_time_minutes = VALUES(estimated_time_minutes)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dist.getFromType());
            stmt.setLong(2, dist.getFromId());
            stmt.setString(3, dist.getToType());
            stmt.setLong(4, dist.getToId());
            stmt.setDouble(5, dist.getDistanceKm());
            stmt.setInt(6, dist.getEstimatedTimeMinutes());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error saving location distance", e);
            throw new DatabaseException("Failed to save location distance", e);
        }
    }
}
