package com.mindlearn.util;

import com.mindlearn.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for common database operations.
 */
public class DBUtil {
    
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());
    
    /**
     * Close ResultSet resources.
     * 
     * @param resultSet the ResultSet to close
     */
    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
    }
    
    /**
     * Close PreparedStatement resources.
     * 
     * @param preparedStatement the PreparedStatement to close
     */
    public static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
        }
    }
    
    /**
     * Close all database resources.
     * 
     * @param resultSet the ResultSet to close
     * @param preparedStatement the PreparedStatement to close
     * @param connection the Connection to close
     */
    public static void close(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        close(resultSet);
        close(preparedStatement);
        DatabaseConfig.closeConnection(connection);
    }
    
    /**
     * Close connection and PreparedStatement.
     * 
     * @param preparedStatement the PreparedStatement to close
     * @param connection the Connection to close
     */
    public static void close(PreparedStatement preparedStatement, Connection connection) {
        close(preparedStatement);
        DatabaseConfig.closeConnection(connection);
    }
    
    /**
     * Execute a SELECT query and return the ResultSet.
     * 
     * @param sql the SQL query
     * @param parameters the query parameters
     * @return ResultSet containing the results
     * @throws SQLException if a database error occurs
     */
    public static ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            
            resultSet = preparedStatement.executeQuery();
            return resultSet;
            
        } catch (SQLException e) {
            close(resultSet, preparedStatement, connection);
            throw e;
        }
    }
    
    /**
     * Execute an INSERT, UPDATE, or DELETE statement.
     * 
     * @param sql the SQL statement
     * @param parameters the statement parameters
     * @return the number of rows affected
     * @throws SQLException if a database error occurs
     */
    public static int executeUpdate(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DatabaseConfig.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            
            return preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            close(preparedStatement, connection);
            throw e;
        }
    }
}