package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.WasteBin;
import com.waste.management.model.WasteType;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WasteBinDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(WasteBinDAO.class);

    public List<WasteBin> findAll(String search, Long areaId, WasteType wasteType, String status) {
        List<WasteBin> bins = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.bin_code, b.area_id, b.location_name, b.address, b.latitude, b.longitude, " +
            "b.waste_type, b.capacity_kg, b.current_fill_percentage, b.status, b.last_collection_at, " +
            "b.last_reported_at, b.created_at, b.updated_at, a.name AS area_name " +
            "FROM waste_bins b " +
            "JOIN areas a ON b.area_id = a.id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (b.bin_code LIKE ? OR b.location_name LIKE ? OR b.address LIKE ?) ");
            String term = "%" + search.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (areaId != null && areaId > 0) {
            sql.append("AND b.area_id = ? ");
            params.add(areaId);
        }
        if (wasteType != null) {
            sql.append("AND b.waste_type = ? ");
            params.add(wasteType.name());
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND b.status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY b.current_fill_percentage DESC, b.bin_code ASC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bins.add(mapResultSetToBin(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error querying bins", e);
            throw new DatabaseException("Failed to query waste bins", e);
        }
        return bins;
    }

    public Optional<WasteBin> findById(Long id) {
        String sql = "SELECT b.id, b.bin_code, b.area_id, b.location_name, b.address, b.latitude, b.longitude, " +
                     "b.waste_type, b.capacity_kg, b.current_fill_percentage, b.status, b.last_collection_at, " +
                     "b.last_reported_at, b.created_at, b.updated_at, a.name AS area_name " +
                     "FROM waste_bins b " +
                     "JOIN areas a ON b.area_id = a.id WHERE b.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBin(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding bin by id: {}", id, e);
            throw new DatabaseException("Failed to find waste bin", e);
        }
        return Optional.empty();
    }

    public Optional<WasteBin> findByBinCode(String binCode) {
        String sql = "SELECT b.id, b.bin_code, b.area_id, b.location_name, b.address, b.latitude, b.longitude, " +
                     "b.waste_type, b.capacity_kg, b.current_fill_percentage, b.status, b.last_collection_at, " +
                     "b.last_reported_at, b.created_at, b.updated_at, a.name AS area_name " +
                     "FROM waste_bins b " +
                     "JOIN areas a ON b.area_id = a.id WHERE b.bin_code = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, binCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBin(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding bin by code: {}", binCode, e);
            throw new DatabaseException("Failed to find waste bin by code", e);
        }
        return Optional.empty();
    }

    public WasteBin save(WasteBin bin) {
        String sql = "INSERT INTO waste_bins (bin_code, area_id, location_name, address, latitude, longitude, " +
                     "waste_type, capacity_kg, current_fill_percentage, status, last_reported_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, bin.getBinCode());
            stmt.setLong(2, bin.getAreaId());
            stmt.setString(3, bin.getLocationName());
            stmt.setString(4, bin.getAddress());
            stmt.setDouble(5, bin.getLatitude());
            stmt.setDouble(6, bin.getLongitude());
            stmt.setString(7, bin.getWasteType().name());
            stmt.setDouble(8, bin.getCapacityKg());
            stmt.setDouble(9, bin.getCurrentFillPercentage());
            stmt.setString(10, bin.getStatus() != null ? bin.getStatus() : "ACTIVE");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bin.setId(keys.getLong(1));
                }
            }
            return bin;
        } catch (SQLException e) {
            LOGGER.error("Error saving bin: {}", bin.getBinCode(), e);
            throw new DatabaseException("Failed to save waste bin", e);
        }
    }

    public boolean update(WasteBin bin) {
        String sql = "UPDATE waste_bins SET bin_code = ?, area_id = ?, location_name = ?, address = ?, " +
                     "latitude = ?, longitude = ?, waste_type = ?, capacity_kg = ?, " +
                     "current_fill_percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bin.getBinCode());
            stmt.setLong(2, bin.getAreaId());
            stmt.setString(3, bin.getLocationName());
            stmt.setString(4, bin.getAddress());
            stmt.setDouble(5, bin.getLatitude());
            stmt.setDouble(6, bin.getLongitude());
            stmt.setString(7, bin.getWasteType().name());
            stmt.setDouble(8, bin.getCapacityKg());
            stmt.setDouble(9, bin.getCurrentFillPercentage());
            stmt.setString(10, bin.getStatus());
            stmt.setLong(11, bin.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating bin: {}", bin.getId(), e);
            throw new DatabaseException("Failed to update waste bin", e);
        }
    }

    public boolean updateFillLevel(Long id, double fillPercentage) {
        String sql = "UPDATE waste_bins SET current_fill_percentage = ?, last_reported_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, fillPercentage);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating bin fill level: {}", id, e);
            throw new DatabaseException("Failed to update fill level", e);
        }
    }

    public boolean deleteOrDeactivate(Long id) {
        // Safe soft delete/deactivate to preserve historical tasks and collection records
        String sql = "UPDATE waste_bins SET status = 'INACTIVE' WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error deactivating bin: {}", id, e);
            throw new DatabaseException("Failed to deactivate bin", e);
        }
    }

    /**
     * Checks if this bin is currently part of an active (PENDING, ASSIGNED, or IN_PROGRESS) collection task.
     * Prevents duplicate active tasks.
     */
    public boolean hasActiveTask(Long binId) {
        String sql = "SELECT COUNT(*) FROM route_stops rs " +
                     "JOIN collection_plans cp ON rs.collection_plan_id = cp.id " +
                     "WHERE rs.waste_bin_id = ? " +
                     "AND cp.status IN ('ASSIGNED', 'IN_PROGRESS', 'DRAFT') " +
                     "AND rs.status = 'PENDING'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, binId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking active tasks for bin: {}", binId, e);
        }
        return false;
    }

    public List<WasteBin> findCriticalBins() {
        return findAll(null, null, null, "ACTIVE").stream()
                .filter(b -> b.getCurrentFillPercentage() >= 90.0)
                .collect(java.util.stream.Collectors.toList());
    }

    private WasteBin mapResultSetToBin(ResultSet rs) throws SQLException {
        WasteBin bin = new WasteBin();
        bin.setId(rs.getLong("id"));
        bin.setBinCode(rs.getString("bin_code"));
        bin.setAreaId(rs.getLong("area_id"));
        bin.setAreaName(rs.getString("area_name"));
        bin.setLocationName(rs.getString("location_name"));
        bin.setAddress(rs.getString("address"));
        bin.setLatitude(rs.getDouble("latitude"));
        bin.setLongitude(rs.getDouble("longitude"));
        bin.setWasteType(WasteType.valueOf(rs.getString("waste_type")));
        bin.setCapacityKg(rs.getDouble("capacity_kg"));
        bin.setCurrentFillPercentage(rs.getDouble("current_fill_percentage"));
        bin.setStatus(rs.getString("status"));

        Timestamp lastCol = rs.getTimestamp("last_collection_at");
        if (lastCol != null) {
            bin.setLastCollectionAt(lastCol.toLocalDateTime());
        }
        Timestamp lastRep = rs.getTimestamp("last_reported_at");
        if (lastRep != null) {
            bin.setLastReportedAt(lastRep.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            bin.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            bin.setUpdatedAt(updated.toLocalDateTime());
        }
        return bin;
    }
}
