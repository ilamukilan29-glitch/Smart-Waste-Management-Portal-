package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.Worker;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkerDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerDAO.class);

    public List<Worker> findAll() {
        List<Worker> workers = new ArrayList<>();
        String sql = "SELECT w.id, w.user_id, w.worker_code, w.full_name, w.phone, w.area_id, w.status, " +
                     "w.availability_status, w.created_at, w.updated_at, a.name AS area_name, u.username " +
                     "FROM workers w " +
                     "JOIN areas a ON w.area_id = a.id " +
                     "LEFT JOIN users u ON w.user_id = u.id " +
                     "ORDER BY w.full_name ASC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                workers.add(mapResultSetToWorker(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all workers", e);
            throw new DatabaseException("Failed to load workers", e);
        }
        return workers;
    }

    public Optional<Worker> findById(Long id) {
        String sql = "SELECT w.id, w.user_id, w.worker_code, w.full_name, w.phone, w.area_id, w.status, " +
                     "w.availability_status, w.created_at, w.updated_at, a.name AS area_name, u.username " +
                     "FROM workers w " +
                     "JOIN areas a ON w.area_id = a.id " +
                     "LEFT JOIN users u ON w.user_id = u.id " +
                     "WHERE w.id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWorker(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding worker by id: {}", id, e);
            throw new DatabaseException("Failed to find worker", e);
        }
        return Optional.empty();
    }

    public Optional<Worker> findByUserId(Long userId) {
        String sql = "SELECT w.id, w.user_id, w.worker_code, w.full_name, w.phone, w.area_id, w.status, " +
                     "w.availability_status, w.created_at, w.updated_at, a.name AS area_name, u.username " +
                     "FROM workers w " +
                     "JOIN areas a ON w.area_id = a.id " +
                     "LEFT JOIN users u ON w.user_id = u.id " +
                     "WHERE w.user_id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWorker(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding worker by user id: {}", userId, e);
            throw new DatabaseException("Failed to find worker for user", e);
        }
        return Optional.empty();
    }

    public List<Worker> findAvailableWorkers(Long areaId) {
        List<Worker> workers = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT w.id, w.user_id, w.worker_code, w.full_name, w.phone, w.area_id, w.status, " +
            "w.availability_status, w.created_at, w.updated_at, a.name AS area_name, u.username " +
            "FROM workers w " +
            "JOIN areas a ON w.area_id = a.id " +
            "LEFT JOIN users u ON w.user_id = u.id " +
            "WHERE w.status = 'ACTIVE' AND w.availability_status = 'AVAILABLE' "
        );
        if (areaId != null && areaId > 0) {
            sql.append("AND w.area_id = ? ");
        }
        sql.append("ORDER BY w.full_name ASC");

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (areaId != null && areaId > 0) {
                stmt.setLong(1, areaId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workers.add(mapResultSetToWorker(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding available workers", e);
            throw new DatabaseException("Failed to load available workers", e);
        }
        return workers;
    }

    public Worker save(Worker worker) {
        String sql = "INSERT INTO workers (user_id, worker_code, full_name, phone, area_id, status, availability_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (worker.getUserId() != null && worker.getUserId() > 0) {
                stmt.setLong(1, worker.getUserId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setString(2, worker.getWorkerCode());
            stmt.setString(3, worker.getFullName());
            stmt.setString(4, worker.getPhone());
            stmt.setLong(5, worker.getAreaId());
            stmt.setString(6, worker.getStatus() != null ? worker.getStatus() : "ACTIVE");
            stmt.setString(7, worker.getAvailabilityStatus() != null ? worker.getAvailabilityStatus() : "AVAILABLE");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    worker.setId(keys.getLong(1));
                }
            }
            return worker;
        } catch (SQLException e) {
            LOGGER.error("Error saving worker: {}", worker.getWorkerCode(), e);
            throw new DatabaseException("Failed to save worker", e);
        }
    }

    public boolean update(Worker worker) {
        String sql = "UPDATE workers SET user_id = ?, worker_code = ?, full_name = ?, phone = ?, " +
                     "area_id = ?, status = ?, availability_status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (worker.getUserId() != null && worker.getUserId() > 0) {
                stmt.setLong(1, worker.getUserId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setString(2, worker.getWorkerCode());
            stmt.setString(3, worker.getFullName());
            stmt.setString(4, worker.getPhone());
            stmt.setLong(5, worker.getAreaId());
            stmt.setString(6, worker.getStatus());
            stmt.setString(7, worker.getAvailabilityStatus());
            stmt.setLong(8, worker.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating worker: {}", worker.getId(), e);
            throw new DatabaseException("Failed to update worker", e);
        }
    }

    public boolean updateAvailability(Long workerId, String availabilityStatus) {
        String sql = "UPDATE workers SET availability_status = ? WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, availabilityStatus);
            stmt.setLong(2, workerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating worker availability: {}", workerId, e);
            throw new DatabaseException("Failed to update worker availability", e);
        }
    }

    private Worker mapResultSetToWorker(ResultSet rs) throws SQLException {
        Worker w = new Worker();
        w.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) {
            w.setUserId(uid);
        }
        w.setUsername(rs.getString("username"));
        w.setWorkerCode(rs.getString("worker_code"));
        w.setFullName(rs.getString("full_name"));
        w.setPhone(rs.getString("phone"));
        w.setAreaId(rs.getLong("area_id"));
        w.setAreaName(rs.getString("area_name"));
        w.setStatus(rs.getString("status"));
        w.setAvailabilityStatus(rs.getString("availability_status"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            w.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            w.setUpdatedAt(updated.toLocalDateTime());
        }
        return w;
    }
}
