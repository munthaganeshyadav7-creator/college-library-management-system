package com.college.library.entity;

/**
 * User roles in the College Library Management System.
 * - STUDENT: Can browse books, borrow available copies, view personal borrowings and penalties.
 * - LIBRARIAN: Can manage books catalog, issue books, process returns, view overdue loans.
 * - ADMIN: Full administrative access, system metrics, users, reports, penalties audit.
 */
public enum Role {
    STUDENT,
    LIBRARIAN,
    ADMIN
}
