package com.mindlearn.dao;

import com.mindlearn.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for mindmap_history table.
 */
public class MindmapDao {

    private static final Logger LOGGER = Logger.getLogger(MindmapDao.class.getName());

    /**
     * Save a mindmap to the database.
     *
     * @param userId      the user ID
     * @param topic       the topic name
     * @param mindmapData the mindmap JSON data
     * @return the generated ID or -1 if failed
     */
    public static int saveMindmap(int userId, String topic, String mindmapData) {
        String sql = "INSERT INTO mindmap_history (user_id, topic, mindmap_data) VALUES (?, ?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, topic);
            preparedStatement.setString(3, mindmapData);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving mindmap", e);
        } finally {
            closeResources(generatedKeys, preparedStatement, connection);
        }
        return -1;
    }

    /**
     * Get all mindmaps for a specific user.
     *
     * @param userId the user ID
     * @return list of mindmap records
     */
    public static List<MindmapRecord> getUserMindmaps(int userId) {
        List<MindmapRecord> mindmaps = new ArrayList<>();
        String sql = "SELECT id, topic, mindmap_data, created_at FROM mindmap_history WHERE user_id = ? ORDER BY created_at DESC";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MindmapRecord record = new MindmapRecord();
                record.setId(resultSet.getInt("id"));
                record.setTopic(resultSet.getString("topic"));
                record.setMindmapData(resultSet.getString("mindmap_data"));
                record.setCreatedAt(resultSet.getTimestamp("created_at"));
                mindmaps.add(record);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user mindmaps", e);
        } finally {
            closeResources(resultSet, preparedStatement, connection);
        }
        return mindmaps;
    }

    /**
     * Get a specific mindmap by ID.
     *
     * @param mindmapId the mindmap ID
     * @param userId    the user ID (for security)
     * @return the mindmap record or null if not found
     */
    public static MindmapRecord getMindmapById(int mindmapId, int userId) {
        String sql = "SELECT id, topic, mindmap_data, created_at FROM mindmap_history WHERE id = ? AND user_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, mindmapId);
            preparedStatement.setInt(2, userId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                MindmapRecord record = new MindmapRecord();
                record.setId(resultSet.getInt("id"));
                record.setTopic(resultSet.getString("topic"));
                record.setMindmapData(resultSet.getString("mindmap_data"));
                record.setCreatedAt(resultSet.getTimestamp("created_at"));
                return record;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving mindmap by ID", e);
        } finally {
            closeResources(resultSet, preparedStatement, connection);
        }
        return null;
    }

    /**
     * Delete a mindmap by ID.
     *
     * @param mindmapId the mindmap ID
     * @param userId    the user ID (for security)
     * @return true if deleted, false otherwise
     */
    public static boolean deleteMindmap(int mindmapId, int userId) {
        String sql = "DELETE FROM mindmap_history WHERE id = ? AND user_id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, mindmapId);
            preparedStatement.setInt(2, userId);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting mindmap", e);
        } finally {
            closeResources(null, preparedStatement, connection);
        }
        return false;
    }

    private static void closeResources(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
        }
        if (connection != null) {
            DatabaseConfig.closeConnection(connection);
        }
    }

    /**
     * Inner class to represent a mindmap record.
     */
    public static class MindmapRecord {
        private int id;
        private String topic;
        private String mindmapData;
        private java.sql.Timestamp createdAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getMindmapData() {
            return mindmapData;
        }

        public void setMindmapData(String mindmapData) {
            this.mindmapData = mindmapData;
        }

        public java.sql.Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.sql.Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }
}