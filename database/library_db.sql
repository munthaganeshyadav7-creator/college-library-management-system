-- ====================================================================
-- College Library Management System - Database Schema & Initial Data
-- B.Tech Technical Training (TT) Session Project
-- Database: MySQL 8.x
-- ====================================================================

CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 1. Users Table
-- Roles: STUDENT, LIBRARIAN, ADMIN
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Books Table
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    CONSTRAINT chk_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_available_quantity CHECK (available_quantity >= 0 AND available_quantity <= quantity)
);

-- 3. Borrow Records Table
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    penalty_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    penalty_paid BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT
);

-- Performance Indexes
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category ON books(category);
CREATE INDEX idx_borrow_student ON borrow_records(student_id);
CREATE INDEX idx_borrow_status ON borrow_records(status);

-- Seed Sample Books
INSERT INTO books (title, author, category, quantity, available_quantity)
SELECT seed.title, seed.author, seed.category, seed.quantity, seed.available_quantity
FROM (
    SELECT 'Introduction to Algorithms' AS title, 'Thomas H. Cormen' AS author, 'Computer Science' AS category, 5 AS quantity, 5 AS available_quantity
    UNION ALL SELECT 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'Software Engineering', 4, 4
    UNION ALL SELECT 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma', 'Software Engineering', 3, 3
    UNION ALL SELECT 'Operating System Concepts', 'Abraham Silberschatz', 'Computer Science', 4, 4
    UNION ALL SELECT 'Database System Concepts', 'Abraham Silberschatz', 'Database Management', 6, 6
    UNION ALL SELECT 'Artificial Intelligence: A Modern Approach', 'Stuart Russell', 'Artificial Intelligence', 3, 3
    UNION ALL SELECT 'Computer Networking: A Top-Down Approach', 'James Kurose', 'Networking', 5, 5
    UNION ALL SELECT 'Java: The Complete Reference', 'Herbert Schildt', 'Programming', 7, 7
    UNION ALL SELECT 'Python Crash Course', 'Eric Matthes', 'Programming', 4, 4
    UNION ALL SELECT 'Cloud Computing: Principles and Paradigms', 'Rajkumar Buyya', 'Cloud Computing', 3, 3
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM books);
