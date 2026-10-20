package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.CollectionRecord;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CollectionRecordDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRecordDAO.class);

    public List<CollectionRecord> findByTaskId(Long taskId) {
        List<CollectionRecord> list = new ArrayList<>();
        String sql = "SELECT cr.id, cr.task_id, cr.waste_bin_id, cr.collected_at, cr.fill_percentage_before, " +
                     "cr.fill_percentage_after, cr.weight_collected_kg, cr.remarks, " +
                     "ct.task_code, b.bin_code, b.location_name " +
                     "FROM collection_records cr " +
                     "JOIN collection_tasks ct ON cr.task_id = ct.id " +
                     "JOIN waste_bins b ON cr.waste_bin_id = b.id " +
                     "WHERE cr.task_id = ? ORDER BY cr.collected_at ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding records for task id: {}", taskId, e);
            throw new DatabaseException("Failed to find collection records", e);
        }
        return list;
    }

    public List<CollectionRecord> findByDateRange(LocalDate start, LocalDate end, Long areaId) {
        List<CollectionRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT cr.id, cr.task_id, cr.waste_bin_id, cr.collected_at, cr.fill_percentage_before, " +
            "cr.fill_percentage_after, cr.weight_collected_kg, cr.remarks, " +
            "ct.task_code, b.bin_code, b.location_name " +
            "FROM collection_records cr " +
            "JOIN collection_tasks ct ON cr.task_id = ct.id " +
            "JOIN waste_bins b ON cr.waste_bin_id = b.id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (start != null) {
            sql.append("AND DATE(cr.collected_at) >= ? ");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            sql.append("AND DATE(cr.collected_at) <= ? ");
            params.add(Date.valueOf(end));
        }
        if (areaId != null && areaId > 0) {
            sql.append("AND b.area_id = ? ");
            params.add(areaId);
        }
        sql.append("ORDER BY cr.collected_at DESC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding records by date range", e);
            throw new DatabaseException("Failed to load collection records", e);
        }
        return list;
    }

    private CollectionRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        CollectionRecord rec = new CollectionRecord();
        rec.setId(rs.getLong("id"));
        rec.setTaskId(rs.getLong("task_id"));
        rec.setTaskCode(rs.getString("task_code"));
        rec.setWasteBinId(rs.getLong("waste_bin_id"));
        rec.setBinCode(rs.getString("bin_code"));
        rec.setLocationName(rs.getString("location_name"));

        Timestamp colTs = rs.getTimestamp("collected_at");
        if (colTs != null) {
            rec.setCollectedAt(colTs.toLocalDateTime());
        }

        rec.setFillPercentageBefore(rs.getDouble("fill_percentage_before"));
        rec.setFillPercentageAfter(rs.getDouble("fill_percentage_after"));
        rec.setWeightCollectedKg(rs.getDouble("weight_collected_kg"));
        rec.setRemarks(rs.getString("remarks"));
        return rec;
    }
}
