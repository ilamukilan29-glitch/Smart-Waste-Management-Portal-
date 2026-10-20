package com.waste.management.dao;

import com.waste.management.exception.DatabaseException;
import com.waste.management.model.Notification;
import com.waste.management.util.DBConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDAO.class);

    public List<Notification> findAll(int limit) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, title, message, alert_type, related_entity_type, related_entity_id, is_read, created_at " +
                     "FROM notifications ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing notifications", e);
            throw new DatabaseException("Failed to load notifications", e);
        }
        return list;
    }

    public List<Notification> findUnread() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, title, message, alert_type, related_entity_type, related_entity_id, is_read, created_at " +
                     "FROM notifications WHERE is_read = FALSE ORDER BY created_at DESC";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error listing unread notifications", e);
            throw new DatabaseException("Failed to load unread notifications", e);
        }
        return list;
    }

    public Notification save(Notification n) {
        String sql = "INSERT INTO notifications (title, message, alert_type, related_entity_type, related_entity_id, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, n.getTitle());
            stmt.setString(2, n.getMessage());
            stmt.setString(3, n.getAlertType());
            stmt.setString(4, n.getRelatedEntityType());
            if (n.getRelatedEntityId() != null) {
                stmt.setLong(5, n.getRelatedEntityId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.setBoolean(6, n.isRead());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setId(keys.getLong(1));
                }
            }
            return n;
        } catch (SQLException e) {
            LOGGER.error("Error saving notification", e);
            throw new DatabaseException("Failed to save notification", e);
        }
    }

    public boolean markAsRead(Long id) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Error marking notification read: {}", id, e);
            throw new DatabaseException("Failed to update notification", e);
        }
    }

    public boolean markAllAsRead() {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE is_read = FALSE";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            LOGGER.error("Error marking all notifications read", e);
            throw new DatabaseException("Failed to update notifications", e);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setAlertType(rs.getString("alert_type"));
        n.setRelatedEntityType(rs.getString("related_entity_type"));
        long relId = rs.getLong("related_entity_id");
        if (!rs.wasNull()) {
            n.setRelatedEntityId(relId);
        }
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            n.setCreatedAt(ts.toLocalDateTime());
        }
        return n;
    }
}
