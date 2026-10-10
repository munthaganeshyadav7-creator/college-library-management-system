package com.college.library.entity;

/**
 * User Roles defined for access control:
 * - STUDENT: Can browse books, borrow available books, view own borrowing history and penalties.
 * - LIBRARIAN: Can manage books, issue books to students, process returns, calculate penalties.
 * - ADMIN: Can view system-wide dashboard stats, reports, penalties, and manage system overview.
 */
public enum Role {
    STUDENT,
    LIBRARIAN,
    ADMIN
}
