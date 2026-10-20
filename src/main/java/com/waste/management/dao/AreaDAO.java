package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.Area;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AreaDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AreaDAO.class);

    public List<Area> findAll() {
        List<Area> areas = new ArrayList<>();
        String sql = "SELECT id, code, name, description, city, created_at FROM areas ORDER BY name ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                areas.add(mapResultSetToArea(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all areas", e);
            throw new DatabaseException("Failed to load areas", e);
        }
        return areas;
    }

    public Optional<Area> findById(Long id) {
        String sql = "SELECT id, code, name, description, city, created_at FROM areas WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToArea(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding area by id: {}", id, e);
            throw new DatabaseException("Failed to find area", e);
        }
        return Optional.empty();
    }

    public Area save(Area area) {
        String sql = "INSERT INTO areas (code, name, description, city) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, area.getCode());
            stmt.setString(2, area.getName());
            stmt.setString(3, area.getDescription());
            stmt.setString(4, area.getCity() != null ? area.getCity() : "Metro City");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    area.setId(keys.getLong(1));
                }
            }
            return area;
        } catch (SQLException e) {
            LOGGER.error("Error saving area: {}", area.getCode(), e);
            throw new DatabaseException("Failed to save area", e);
        }
    }

    private Area mapResultSetToArea(ResultSet rs) throws SQLException {
        Area area = new Area();
        area.setId(rs.getLong("id"));
        area.setCode(rs.getString("code"));
        area.setName(rs.getString("name"));
        area.setDescription(rs.getString("description"));
        area.setCity(rs.getString("city"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            area.setCreatedAt(ts.toLocalDateTime());
        }
        return area;
    }
}
