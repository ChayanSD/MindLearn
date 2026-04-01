-- MindLearn Database Schema
-- Run this SQL to create the database and tables

-- Create database
CREATE DATABASE IF NOT EXISTS mindlearn_db;
USE mindlearn_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create mindmap_history table to store user generated mindmaps
CREATE TABLE IF NOT EXISTS mindmap_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    mindmap_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Optional: Insert a test user
-- INSERT INTO users (email, password) VALUES ('test@example.com', 'password123');