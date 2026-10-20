package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.CollectionRecord;
import com.waste.management.model.CollectionTask;
import com.waste.management.model.TaskStatus;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectionTaskDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTaskDAO.class);

    public CollectionTask save(CollectionTask task) {
        String sql = "INSERT INTO collection_tasks (task_code, collection_plan_id, worker_id, vehicle_id, " +
                     "planned_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getTaskCode());
            stmt.setLong(2, task.getCollectionPlanId());
            stmt.setLong(3, task.getWorkerId());
            stmt.setLong(4, task.getVehicleId());
            stmt.setDate(5, Date.valueOf(task.getPlannedDate()));
            stmt.setString(6, task.getStatus().name());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getLong(1));
                }
            }
            return task;
        } catch (SQLException e) {
            LOGGER.error("Error creating collection task", e);
            throw new DatabaseException("Failed to save collection task", e);
        }
    }

    public Optional<CollectionTask> findById(Long id) {
        String sql = "SELECT ct.id, ct.task_code, ct.collection_plan_id, ct.worker_id, ct.vehicle_id, " +
                     "ct.planned_date, ct.actual_start_at, ct.actual_completed_at, ct.collected_weight_kg, " +
                     "ct.remarks, ct.status, ct.created_at, ct.updated_at, " +
                     "cp.plan_code, w.full_name AS worker_name, v.registration_number AS vehicle_registration " +
                     "FROM collection_tasks ct " +
                     "JOIN collection_plans cp ON ct.collection_plan_id = cp.id " +
                     "JOIN workers w ON ct.worker_id = w.id " +
                     "JOIN vehicles v ON ct.vehicle_id = v.id " +
                     "WHERE ct.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding task by id: {}", id, e);
            throw new DatabaseException("Failed to find collection task", e);
        }
        return Optional.empty();
    }

    public List<CollectionTask> findByWorkerId(Long workerId) {
        List<CollectionTask> tasks = new ArrayList<>();
        String sql = "SELECT ct.id, ct.task_code, ct.collection_plan_id, ct.worker_id, ct.vehicle_id, " +
                     "ct.planned_date, ct.actual_start_at, ct.actual_completed_at, ct.collected_weight_kg, " +
                     "ct.remarks, ct.status, ct.created_at, ct.updated_at, " +
                     "cp.plan_code, w.full_name AS worker_name, v.registration_number AS vehicle_registration " +
                     "FROM collection_tasks ct " +
                     "JOIN collection_plans cp ON ct.collection_plan_id = cp.id " +
                     "JOIN workers w ON ct.worker_id = w.id " +
                     "JOIN vehicles v ON ct.vehicle_id = v.id " +
                     "WHERE ct.worker_id = ? " +
                     "ORDER BY ct.planned_date DESC, ct.id DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, workerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding tasks by worker: {}", workerId, e);
            throw new DatabaseException("Failed to find worker tasks", e);
        }
        return tasks;
    }

    public List<CollectionTask> findAll() {
        List<CollectionTask> tasks = new ArrayList<>();
        String sql = "SELECT ct.id, ct.task_code, ct.collection_plan_id, ct.worker_id, ct.vehicle_id, " +
                     "ct.planned_date, ct.actual_start_at, ct.actual_completed_at, ct.collected_weight_kg, " +
                     "ct.remarks, ct.status, ct.created_at, ct.updated_at, " +
                     "cp.plan_code, w.full_name AS worker_name, v.registration_number AS vehicle_registration " +
                     "FROM collection_tasks ct " +
                     "JOIN collection_plans cp ON ct.collection_plan_id = cp.id " +
                     "JOIN workers w ON ct.worker_id = w.id " +
                     "JOIN vehicles v ON ct.vehicle_id = v.id " +
                     "ORDER BY ct.planned_date DESC, ct.id DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all tasks", e);
            throw new DatabaseException("Failed to find tasks", e);
        }
        return tasks;
    }

    public boolean updateStatus(Long taskId, TaskStatus status) {
        String sql = "UPDATE collection_tasks SET status = ?, " +
                     "actual_start_at = CASE WHEN ? = 'IN_PROGRESS' AND actual_start_at IS NULL THEN CURRENT_TIMESTAMP ELSE actual_start_at END " +
                     "WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, status.name());
            stmt.setLong(3, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating task status: {}", taskId, e);
            throw new DatabaseException("Failed to update task status", e);
        }
    }

    /**
     * Completes a task transactionally:
     * 1. Updates task status to COMPLETED with actual weight and remarks.
     * 2. Updates collection plan status to COMPLETED.
     * 3. Inserts individual bin collection records.
     * 4. Updates bin fill levels to 0.0% and sets last_collection_at.
     * 5. Sets route stops status to VISITED.
     * 6. Sets worker and vehicle availability back to AVAILABLE.
     */
    public boolean completeTask(Long taskId, double collectedWeightKg, String remarks, List<CollectionRecord> records) {
        Connection conn = null;
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false); // Begin multi-table write transaction

            // 1. Get task details (worker, vehicle, plan)
            Long planId = null;
            Long workerId = null;
            Long vehicleId = null;
            String queryTask = "SELECT collection_plan_id, worker_id, vehicle_id FROM collection_tasks WHERE id = ?";
            try (PreparedStatement qStmt = conn.prepareStatement(queryTask)) {
                qStmt.setLong(1, taskId);
                try (ResultSet rs = qStmt.executeQuery()) {
                    if (rs.next()) {
                        planId = rs.getLong("collection_plan_id");
                        workerId = rs.getLong("worker_id");
                        vehicleId = rs.getLong("vehicle_id");
                    } else {
                        throw new DatabaseException("Task not found with id: " + taskId);
                    }
                }
            }

            // 2. Update task
            String updateTaskSql = "UPDATE collection_tasks SET status = 'COMPLETED', " +
                                  "actual_completed_at = CURRENT_TIMESTAMP, " +
                                  "collected_weight_kg = ?, remarks = ? WHERE id = ?";
            try (PreparedStatement tStmt = conn.prepareStatement(updateTaskSql)) {
                tStmt.setDouble(1, collectedWeightKg);
                tStmt.setString(2, remarks);
                tStmt.setLong(3, taskId);
                tStmt.executeUpdate();
            }

            // 3. Update collection plan
            if (planId != null) {
                String updatePlanSql = "UPDATE collection_plans SET status = 'COMPLETED' WHERE id = ?";
                try (PreparedStatement pStmt = conn.prepareStatement(updatePlanSql)) {
                    pStmt.setLong(1, planId);
                    pStmt.executeUpdate();
                }

                // Update all route stops to VISITED
                String updateStopsSql = "UPDATE route_stops SET status = 'VISITED' WHERE collection_plan_id = ?";
                try (PreparedStatement sStmt = conn.prepareStatement(updateStopsSql)) {
                    sStmt.setLong(1, planId);
                    sStmt.executeUpdate();
                }
            }

            // 4. Insert collection records & update waste bins
            if (records != null && !records.isEmpty()) {
                String insertRecSql = "INSERT INTO collection_records (task_id, waste_bin_id, fill_percentage_before, " +
                                      "fill_percentage_after, weight_collected_kg, remarks) VALUES (?, ?, ?, ?, ?, ?)";
                String updateBinSql = "UPDATE waste_bins SET current_fill_percentage = 0.0, " +
                                      "last_collection_at = CURRENT_TIMESTAMP WHERE id = ?";

                try (PreparedStatement rStmt = conn.prepareStatement(insertRecSql);
                     PreparedStatement bStmt = conn.prepareStatement(updateBinSql)) {

                    for (CollectionRecord rec : records) {
                        rStmt.setLong(1, taskId);
                        rStmt.setLong(2, rec.getWasteBinId());
                        rStmt.setDouble(3, rec.getFillPercentageBefore());
                        rStmt.setDouble(4, 0.0); // Reset to 0% after collection
                        rStmt.setDouble(5, rec.getWeightCollectedKg());
                        rStmt.setString(6, rec.getRemarks());
                        rStmt.addBatch();

                        bStmt.setLong(1, rec.getWasteBinId());
                        bStmt.addBatch();
                    }
                    rStmt.executeBatch();
                    bStmt.executeBatch();
                }
            }

            // 5. Release worker and vehicle availability
            if (workerId != null) {
                String releaseWorkerSql = "UPDATE workers SET availability_status = 'AVAILABLE' WHERE id = ?";
                try (PreparedStatement wStmt = conn.prepareStatement(releaseWorkerSql)) {
                    wStmt.setLong(1, workerId);
                    wStmt.executeUpdate();
                }
            }

            if (vehicleId != null) {
                String releaseVehicleSql = "UPDATE vehicles SET availability_status = 'AVAILABLE' WHERE id = ?";
                try (PreparedStatement vStmt = conn.prepareStatement(releaseVehicleSql)) {
                    vStmt.setLong(1, vehicleId);
                    vStmt.executeUpdate();
                }
            }

            conn.commit(); // Transaction success!
            LOGGER.info("Task #{} marked COMPLETED successfully with all transactional updates.", taskId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error("Rollback failed", ex);
                }
            }
            LOGGER.error("Error completing task #{}: {}", taskId, e.getMessage(), e);
            throw new DatabaseException("Failed to complete task transactionally", e);
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

    private CollectionTask mapResultSetToTask(ResultSet rs) throws SQLException {
        CollectionTask task = new CollectionTask();
        task.setId(rs.getLong("id"));
        task.setTaskCode(rs.getString("task_code"));
        task.setCollectionPlanId(rs.getLong("collection_plan_id"));
        task.setPlanCode(rs.getString("plan_code"));
        task.setWorkerId(rs.getLong("worker_id"));
        task.setWorkerName(rs.getString("worker_name"));
        task.setVehicleId(rs.getLong("vehicle_id"));
        task.setVehicleRegistration(rs.getString("vehicle_registration"));

        Date plannedDate = rs.getDate("planned_date");
        if (plannedDate != null) {
            task.setPlannedDate(plannedDate.toLocalDate());
        }

        Timestamp start = rs.getTimestamp("actual_start_at");
        if (start != null) {
            task.setActualStartAt(start.toLocalDateTime());
        }

        Timestamp completed = rs.getTimestamp("actual_completed_at");
        if (completed != null) {
            task.setActualCompletedAt(completed.toLocalDateTime());
        }

        task.setCollectedWeightKg(rs.getDouble("collected_weight_kg"));
        task.setRemarks(rs.getString("remarks"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            task.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            task.setUpdatedAt(updated.toLocalDateTime());
        }

        return task;
    }
}
