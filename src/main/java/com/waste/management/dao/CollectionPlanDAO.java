package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.CollectionPlan;
import com.waste.management.model.RouteStop;
import com.waste.management.model.TaskStatus;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectionPlanDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionPlanDAO.class);

    public CollectionPlan save(CollectionPlan plan, List<RouteStop> stops) {
        String planSql = "INSERT INTO collection_plans (plan_code, area_id, depot_id, vehicle_id, worker_id, " +
                         "total_distance_km, estimated_duration_minutes, total_waste_estimated_kg, status, " +
                         "planned_collection_date, created_by_user_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String stopSql = "INSERT INTO route_stops (collection_plan_id, stop_order, stop_type, waste_bin_id, " +
                         "location_label, distance_from_prev_km, cumulative_distance_km, expected_waste_kg, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Insert collection plan
            try (PreparedStatement planStmt = conn.prepareStatement(planSql, Statement.RETURN_GENERATED_KEYS)) {
                planStmt.setString(1, plan.getPlanCode());
                planStmt.setLong(2, plan.getAreaId());
                planStmt.setLong(3, plan.getDepotId());

                if (plan.getVehicleId() != null && plan.getVehicleId() > 0) {
                    planStmt.setLong(4, plan.getVehicleId());
                } else {
                    planStmt.setNull(4, Types.BIGINT);
                }

                if (plan.getWorkerId() != null && plan.getWorkerId() > 0) {
                    planStmt.setLong(5, plan.getWorkerId());
                } else {
                    planStmt.setNull(5, Types.BIGINT);
                }

                planStmt.setDouble(6, plan.getTotalDistanceKm());
                planStmt.setInt(7, plan.getEstimatedDurationMinutes());
                planStmt.setDouble(8, plan.getTotalWasteEstimatedKg());
                planStmt.setString(9, plan.getStatus().name());
                planStmt.setDate(10, Date.valueOf(plan.getPlannedCollectionDate()));
                planStmt.setLong(11, plan.getCreatedByUserId());

                planStmt.executeUpdate();
                try (ResultSet keys = planStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        plan.setId(keys.getLong(1));
                    }
                }
            }

            // 2. Insert sequential route stops
            try (PreparedStatement stopStmt = conn.prepareStatement(stopSql, Statement.RETURN_GENERATED_KEYS)) {
                for (RouteStop stop : stops) {
                    stopStmt.setLong(1, plan.getId());
                    stopStmt.setInt(2, stop.getStopOrder());
                    stopStmt.setString(3, stop.getStopType());
                    if (stop.getWasteBinId() != null && stop.getWasteBinId() > 0) {
                        stopStmt.setLong(4, stop.getWasteBinId());
                    } else {
                        stopStmt.setNull(4, Types.BIGINT);
                    }
                    stopStmt.setString(5, stop.getLocationLabel());
                    stopStmt.setDouble(6, stop.getDistanceFromPrevKm());
                    stopStmt.setDouble(7, stop.getCumulativeDistanceKm());
                    stopStmt.setDouble(8, stop.getExpectedWasteKg());
                    stopStmt.setString(9, stop.getStatus() != null ? stop.getStatus() : "PENDING");

                    stopStmt.addBatch();
                }
                stopStmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            plan.setStops(stops);
            return plan;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error("Rollback failed", ex);
                }
            }
            LOGGER.error("Error saving collection plan transactionally", e);
            throw new DatabaseException("Failed to save collection plan", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    LOGGER.error("Error closing connection", ex);
                }
            }
        }
    }

    public Optional<CollectionPlan> findById(Long id) {
        String sql = "SELECT cp.id, cp.plan_code, cp.area_id, cp.depot_id, cp.vehicle_id, cp.worker_id, " +
                     "cp.total_distance_km, cp.estimated_duration_minutes, cp.total_waste_estimated_kg, " +
                     "cp.status, cp.planned_collection_date, cp.created_by_user_id, cp.created_at, cp.updated_at, " +
                     "a.name AS area_name, d.name AS depot_name, v.registration_number AS vehicle_registration, " +
                     "w.full_name AS worker_name, u.full_name AS creator_name " +
                     "FROM collection_plans cp " +
                     "JOIN areas a ON cp.area_id = a.id " +
                     "JOIN depots d ON cp.depot_id = d.id " +
                     "LEFT JOIN vehicles v ON cp.vehicle_id = v.id " +
                     "LEFT JOIN workers w ON cp.worker_id = w.id " +
                     "JOIN users u ON cp.created_by_user_id = u.id " +
                     "WHERE cp.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CollectionPlan plan = mapResultSetToPlan(rs);
                    plan.setStops(findStopsByPlanId(plan.getId(), conn));
                    return Optional.of(plan);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding collection plan by id: {}", id, e);
            throw new DatabaseException("Failed to find collection plan", e);
        }
        return Optional.empty();
    }

    public List<CollectionPlan> findAll() {
        List<CollectionPlan> plans = new ArrayList<>();
        String sql = "SELECT cp.id, cp.plan_code, cp.area_id, cp.depot_id, cp.vehicle_id, cp.worker_id, " +
                     "cp.total_distance_km, cp.estimated_duration_minutes, cp.total_waste_estimated_kg, " +
                     "cp.status, cp.planned_collection_date, cp.created_by_user_id, cp.created_at, cp.updated_at, " +
                     "a.name AS area_name, d.name AS depot_name, v.registration_number AS vehicle_registration, " +
                     "w.full_name AS worker_name, u.full_name AS creator_name " +
                     "FROM collection_plans cp " +
                     "JOIN areas a ON cp.area_id = a.id " +
                     "JOIN depots d ON cp.depot_id = d.id " +
                     "LEFT JOIN vehicles v ON cp.vehicle_id = v.id " +
                     "LEFT JOIN workers w ON cp.worker_id = w.id " +
                     "JOIN users u ON cp.created_by_user_id = u.id " +
                     "ORDER BY cp.created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing collection plans", e);
            throw new DatabaseException("Failed to list collection plans", e);
        }
        return plans;
    }

    public boolean updateStatus(Long id, TaskStatus status) {
        String sql = "UPDATE collection_plans SET status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating plan status: {}", id, e);
            throw new DatabaseException("Failed to update plan status", e);
        }
    }

    public List<RouteStop> findStopsByPlanId(Long planId, Connection conn) throws SQLException {
        List<RouteStop> stops = new ArrayList<>();
        String sql = "SELECT rs.id, rs.collection_plan_id, rs.stop_order, rs.stop_type, rs.waste_bin_id, " +
                     "rs.location_label, rs.distance_from_prev_km, rs.cumulative_distance_km, " +
                     "rs.expected_waste_kg, rs.status, rs.arrival_time, rs.departure_time, b.bin_code " +
                     "FROM route_stops rs " +
                     "LEFT JOIN waste_bins b ON rs.waste_bin_id = b.id " +
                     "WHERE rs.collection_plan_id = ? ORDER BY rs.stop_order ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RouteStop stop = new RouteStop();
                    stop.setId(rs.getLong("id"));
                    stop.setCollectionPlanId(rs.getLong("collection_plan_id"));
                    stop.setStopOrder(rs.getInt("stop_order"));
                    stop.setStopType(rs.getString("stop_type"));
                    long binId = rs.getLong("waste_bin_id");
                    if (!rs.wasNull()) {
                        stop.setWasteBinId(binId);
                    }
                    stop.setBinCode(rs.getString("bin_code"));
                    stop.setLocationLabel(rs.getString("location_label"));
                    stop.setDistanceFromPrevKm(rs.getDouble("distance_from_prev_km"));
                    stop.setCumulativeDistanceKm(rs.getDouble("cumulative_distance_km"));
                    stop.setExpectedWasteKg(rs.getDouble("expected_waste_kg"));
                    stop.setStatus(rs.getString("status"));
                    stops.add(stop);
                }
            }
        }
        return stops;
    }

    private CollectionPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        CollectionPlan plan = new CollectionPlan();
        plan.setId(rs.getLong("id"));
        plan.setPlanCode(rs.getString("plan_code"));
        plan.setAreaId(rs.getLong("area_id"));
        plan.setAreaName(rs.getString("area_name"));
        plan.setDepotId(rs.getLong("depot_id"));
        plan.setDepotName(rs.getString("depot_name"));

        long vehId = rs.getLong("vehicle_id");
        if (!rs.wasNull()) plan.setVehicleId(vehId);
        plan.setVehicleRegistration(rs.getString("vehicle_registration"));

        long wrkId = rs.getLong("worker_id");
        if (!rs.wasNull()) plan.setWorkerId(wrkId);
        plan.setWorkerName(rs.getString("worker_name"));

        plan.setTotalDistanceKm(rs.getDouble("total_distance_km"));
        plan.setEstimatedDurationMinutes(rs.getInt("estimated_duration_minutes"));
        plan.setTotalWasteEstimatedKg(rs.getDouble("total_waste_estimated_kg"));
        plan.setStatus(TaskStatus.valueOf(rs.getString("status")));

        Date plannedDate = rs.getDate("planned_collection_date");
        if (plannedDate != null) plan.setPlannedCollectionDate(plannedDate.toLocalDate());

        plan.setCreatedByUserId(rs.getLong("created_by_user_id"));
        plan.setCreatedByUserName(rs.getString("creator_name"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) plan.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) plan.setUpdatedAt(updated.toLocalDateTime());

        return plan;
    }
}
