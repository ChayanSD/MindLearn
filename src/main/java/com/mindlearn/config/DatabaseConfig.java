package com.mindlearn.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database configuration class that manages MySQL database connections.
 * Loads configuration from db.properties file.
 */
public class DatabaseConfig {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String PROPERTIES_FILE = "db.properties";
    
    private static String driver;
    private static String url;
    private static String username;
    private static String password;
    
    static {
        loadDatabaseProperties();
    }
    
    /**
     * Load database properties from the configuration file.
     */
    private static void loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            
            if (input == null) {
                LOGGER.severe("Unable to find " + PROPERTIES_FILE);
                return;
            }
            
            props.load(input);
            
            driver = props.getProperty("db.driver");
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            
            // Load the JDBC driver
            Class.forName(driver);
            
            LOGGER.info("Database configuration loaded successfully");
            
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error loading database configuration", e);
        }
    }
    
    /**
     * Get a database connection.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Close a database connection.
     * 
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
    
    /**
     * Test the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
}