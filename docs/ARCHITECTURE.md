# College Library Management System - Architecture & Design

This document details the system architecture, design patterns, security model, and component interactions of the College Library Management System developed for the B.Tech Technical Training (TT) Session Project.

---

## 1. High-Level Architectural Overview

The application follows the classic **Layered Client-Server Architecture** (Model-View-Controller / 3-Tier Enterprise Pattern):

```
+---------------------------------------------------------------------------------+
|                                 FRONTEND TIER                                   |
|   HTML5 + Vanilla CSS3 + Modern ES6 JavaScript (Fetch API, DOM manipulation)    |
+---------------------------------------------------------------------------------+
                                       |
                                       | HTTP / REST (JSON)
                                       v
+---------------------------------------------------------------------------------+
|                                 BACKEND TIER                                    |
|                              Spring Boot 3.x                                    |
|                                                                                 |
|   [Security Filter Chain]  ->  Authentication / Authorization (BCrypt, RBAC)    |
|              |                                                                  |
|              v                                                                  |
|   [REST Controllers]       ->  Endpoint Routing & Request Validation (DTOs)     |
|              |                                                                  |
|              v                                                                  |
|   [Service Layer]          ->  Business Logic & Transaction Management          |
|              |                 (Issue, Return, ₹5/day Penalty Calculation)      |
|              v                                                                  |
|   [JPA Repositories]       ->  Spring Data JPA / Hibernate Object-Relational    |
+---------------------------------------------------------------------------------+
                                       |
                                       | JDBC (MySQL Driver)
                                       v
+---------------------------------------------------------------------------------+
|                                DATABASE TIER                                    |
|                                 MySQL 8.x                                       |
|               Tables: users, books, borrow_records (Foreign Keys)               |
+---------------------------------------------------------------------------------+
```

---

## 2. Layer-by-Layer Breakdown

### A. Presentation Layer (Frontend)
- **Role:** Pure HTML5 structure, semantic markup, responsive CSS, and Vanilla JavaScript.
- **Independence:** Decoupled from backend rendering engines (no JSP or Thymeleaf tight coupling); consumes standard REST APIs via JavaScript `fetch()`.
- **State Management:** User details and role stored in `localStorage` for responsive client-side UI rendering, while actual authentication context is protected via backend sessions.
- **Role-based Views:**
  - `student-dashboard.html`: Loan status, catalog search, borrowing, overdue warnings.
  - `librarian-dashboard.html`: Book circulation, issue/return desk, inventory counters.
  - `admin-dashboard.html`: Institutional KPIs, total inventory volume, fee ledgers, user lists.

### B. Controller Layer (Spring REST Controllers)
- **Role:** Exposes RESTful endpoints, handles HTTP methods (GET, POST, PUT, DELETE), validates input payloads using `@Valid`, and wraps responses in `ApiResponse<T>` / `ResponseEntity`.
- **Controllers:**
  - `AuthController`: Handles registration (`/api/register`), login (`/api/login`), logout (`/api/logout`), and status (`/api/auth/me`).
  - `BookController`: Handles catalog browsing, search (`/api/books?search=...`), and CRUD inventory updates (`/api/books`).
  - `BorrowController`: Handles borrowing (`/api/borrow`), returns (`/api/borrow/{id}/return`), and overdue queries (`/api/borrow/overdue`).
  - `DashboardController`: Aggregates statistics (`/api/dashboard`) and reports (`/api/reports/**`).
  - `UserController`: Provides student listings for circulation desk dropdowns (`/api/students`).

### C. Service Layer (Business Logic)
- **Role:** Implements domain rules, coordinates transactions (`@Transactional`), and encapsulates core logic:
  1. **Borrow Rule:** Checks active account status, verifies `available_quantity > 0`, issues book, sets 7-day due date, decrements available stock.
  2. **Return Rule:** Validates that record is not already returned, records actual return date, calculates late penalty if `returnDate > dueDate` (`overdueDays * ₹5.0`), increments available stock, marks status `RETURNED`.
  3. **Penalty Calculation:**
     $$\text{Overdue Days} = \text{Current Date} - \text{Due Date}$$
     $$\text{Estimated / Final Penalty} = \text{Overdue Days} \times ₹5.00$$

### D. Data Persistence Layer (Spring Data JPA)
- **Repositories:** Interfaces extending `JpaRepository<Entity, Long>`.
- **Hibernate ORM:** Automatically maps Java objects to MySQL relational tables.
- **Custom Queries:** Uses JPQL `@Query` annotations for analytical aggregations (total copies, overdue counts, sum of penalties).

### E. Database Layer (MySQL 8.x)
- **Database:** `library_db`
- **Integrity Constraints:**
  - Unique constraint on `users.email`.
  - Foreign key relations: `borrow_records.student_id -> users.id`, `borrow_records.book_id -> books.id`.
  - Non-negative stock check constraints.

---

## 3. Security Architecture & Access Control

```
                 Incoming Request
                        |
                        v
       [CorsFilter / SecurityFilterChain]
                        |
        +---------------+---------------+
        |                               |
  Public Endpoint                Protected Endpoint
(e.g., /api/login,               (e.g., /api/borrow,
 /api/books [GET])                /api/books [POST])
        |                               |
        | Permit                        v
        +--------------->   [Authentication Check]
                                        |
                            +-----------+-----------+
                            |                       |
                      Unauthenticated         Authenticated
                            |                       |
                            v                       v
                        401 Unauthorized    [Role Authority Check]
                                                    |
                                            +-------+-------+
                                            |               |
                                     Role Matches     Role Mismatch
                                            |               |
                                            v               v
                                         Execute     403 Forbidden
```

### Roles and Permissions Matrix
| Endpoint | Method | Public | Student | Librarian | Admin |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `/api/register` | POST | Yes | - | - | - |
| `/api/login` | POST | Yes | Yes | Yes | Yes |
| `/api/books` | GET | Yes | Yes | Yes | Yes |
| `/api/borrow` | POST | No | Yes (self) | Yes | Yes |
| `/api/borrow/my` | GET | No | Yes | No | No |
| `/api/borrow/{id}/return` | PUT | No | No | Yes | Yes |
| `/api/books` (Add/Edit/Del) | POST/PUT/DEL | No | No | Yes | Yes |
| `/api/borrow/overdue` | GET | No | No | Yes | Yes |
| `/api/dashboard` | GET | No | No | Yes | Yes |
| `/api/dashboard/penalties` | GET | No | No | Yes | Yes |
| `/api/reports/**` | GET | No | No | No | Yes |

---

## 4. Entity-Relationship Diagram (ERD)

```
+--------------------+        1:N         +------------------------+
|       users        |<-------------------|     borrow_records     |
+--------------------+                    +------------------------+
| id (PK)            |                    | id (PK)                |
| name               |                    | student_id (FK)        |
| email (UNIQUE)     |                    | book_id (FK)           |
| password_hash      |                    | issue_date             |
| phone              |                    | due_date               |
| role (ENUM)        |                    | return_date            |
| active             |                    | status                 |
| created_at         |                    | penalty_amount         |
+--------------------+                    | penalty_paid           |
                                          +------------------------+
                                                       |
+--------------------+        1:N                      |
|       books        |<--------------------------------+
+--------------------+
| id (PK)            |
| title              |
| author             |
| category           |
| quantity           |
| available_quantity |
+--------------------+
```
