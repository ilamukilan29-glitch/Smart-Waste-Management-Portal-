package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.Depot;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepotDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepotDAO.class);

    public List<Depot> findAll() {
        List<Depot> depots = new ArrayList<>();
        String sql = "SELECT d.id, d.area_id, d.code, d.name, d.address, d.latitude, d.longitude, d.contact_phone, d.created_at, " +
                     "a.name AS area_name " +
                     "FROM depots d " +
                     "JOIN areas a ON d.area_id = a.id " +
                     "ORDER BY d.name ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                depots.add(mapResultSetToDepot(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all depots", e);
            throw new DatabaseException("Failed to load depots", e);
        }
        return depots;
    }

    public Optional<Depot> findById(Long id) {
        String sql = "SELECT d.id, d.area_id, d.code, d.name, d.address, d.latitude, d.longitude, d.contact_phone, d.created_at, " +
                     "a.name AS area_name " +
                     "FROM depots d " +
                     "JOIN areas a ON d.area_id = a.id " +
                     "WHERE d.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepot(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding depot by id: {}", id, e);
            throw new DatabaseException("Failed to find depot", e);
        }
        return Optional.empty();
    }

    public List<Depot> findByAreaId(Long areaId) {
        List<Depot> depots = new ArrayList<>();
        String sql = "SELECT d.id, d.area_id, d.code, d.name, d.address, d.latitude, d.longitude, d.contact_phone, d.created_at, " +
                     "a.name AS area_name " +
                     "FROM depots d " +
                     "JOIN areas a ON d.area_id = a.id " +
                     "WHERE d.area_id = ? ORDER BY d.name ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, areaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    depots.add(mapResultSetToDepot(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding depots by area id: {}", areaId, e);
            throw new DatabaseException("Failed to find depots by area", e);
        }
        return depots;
    }

    private Depot mapResultSetToDepot(ResultSet rs) throws SQLException {
        Depot depot = new Depot();
        depot.setId(rs.getLong("id"));
        depot.setAreaId(rs.getLong("area_id"));
        depot.setAreaName(rs.getString("area_name"));
        depot.setCode(rs.getString("code"));
        depot.setName(rs.getString("name"));
        depot.setAddress(rs.getString("address"));
        depot.setLatitude(rs.getDouble("latitude"));
        depot.setLongitude(rs.getDouble("longitude"));
        depot.setContactPhone(rs.getString("contact_phone"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            depot.setCreatedAt(ts.toLocalDateTime());
        }
        return depot;
    }
}
