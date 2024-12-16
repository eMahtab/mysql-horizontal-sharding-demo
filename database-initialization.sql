-- Initialize the test database and tables if they do not exist

-- Create the test database if it doesn't exist
CREATE DATABASE IF NOT EXISTS test;
USE test;

-- Create the 'users' table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    name VARCHAR(50),
    username VARCHAR(30),
    PRIMARY KEY (id)
);

-- Create the 'messages' table if it doesn't exist
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT NOT NULL,
    sender_id BIGINT,
    recipient_id BIGINT,
    message TEXT,
    created_at DATETIME NOT NULL,
    edited_at DATETIME DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);