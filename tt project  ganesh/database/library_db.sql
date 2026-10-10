-- ==========================================================
-- COLLEGE LIBRARY MANAGEMENT SYSTEM DATABASE SCRIPT
-- Database: library_db
-- Target Engine: MySQL 8.x
-- ==========================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- Drop tables if they exist (in dependency order)
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- 'STUDENT', 'LIBRARIAN', 'ADMIN'
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Books Table
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1
);

-- 3. Borrow Records Table
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED', -- 'ISSUED', 'RETURNED', 'OVERDUE'
    penalty_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    penalty_paid BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Initial seed data (BCrypt encoded passwords for dev accounts)
-- Admin@123     -> $2a$10$w8T06Q4G7qj6s2m4Y.jI1uWp35csk1P0xI0g2a230J4vW7mO2j3cO (or generated via Spring Security)
-- Lib@12345     -> $2a$10$iMhK2u8RzDq4s1l9P7eDu.c1zF6Q7qA7R8mU4j6yM0vP7kO9m1h3S
-- Student@123   -> $2a$10$7vM7j5m2F7zY4r0w0y3S8uV1m6g8w9v4h7n2b1a3d5e7f9g1h3j5k

-- Note: The Spring Boot application will automatically initialize the demo accounts 
-- and default book catalog on application startup using BCrypt hashing if not present.
