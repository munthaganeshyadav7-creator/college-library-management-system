# College Library Management System - Architecture & Technical Specification

## 1. High-Level Architecture Overview

The College Library Management System is built following a clean, decoupled 3-tier monolithic enterprise architecture:

```
+-----------------------------------------------------------------------------------+
|                            PRESENTATION TIER (FRONTEND)                           |
|  - HTML5 Responsive Views (11 distinct role pages)                                |
|  - Vanilla JavaScript (Fetch API, Token Session Manager, Dynamic DOM)             |
|  - Modern Navy Blue Responsive CSS                                                |
+------------------------------------------+----------------------------------------+
                                           | HTTP / REST (JSON)
                                           | Authorization: Bearer <JWT>
+------------------------------------------v----------------------------------------+
|                            APPLICATION TIER (SPRING BOOT 3)                       |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  | Security & Filter Layer: SecurityConfig, JwtFilter, BCryptPasswordEncoder   |  |
|  +-----------------------------------------------------------------------------+  |
|  | Controller Layer: AuthController, BookController, BorrowController, etc.   |  |
|  +-----------------------------------------------------------------------------+  |
|  | Service Layer: Business Logic, 7-Day Loan Rule, ₹5/Day Overdue Calculation  |  |
|  +-----------------------------------------------------------------------------+  |
|  | Data Access Layer: Spring Data JPA (UserRepository, BookRepository, etc.)  |  |
|  +-----------------------------------------------------------------------------+  |
+------------------------------------------+----------------------------------------+
                                           | JDBC / Hibernate ORM
+------------------------------------------v----------------------------------------+
|                            DATA PERSISTENCE TIER (MySQL 8)                        |
|  - library_db (users, books, borrow_records)                                      |
+-----------------------------------------------------------------------------------+
```

---

## 2. Layered Component Responsibilities

| Layer | Primary Classes | Key Responsibilities |
| :--- | :--- | :--- |
| **Presentation (UI)** | HTML5, CSS3, `script.js` | UI rendering, client input validation, role-based screen routing, asynchronous REST consumption. |
| **Security & Filter** | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil` | Request interception, JWT token parsing & validation, RBAC route guarding, CORS policy. |
| **REST Controllers** | `AuthController`, `BookController`, `BorrowController`, `DashboardController`, `ReportController` | HTTP request handling, DTO deserialization, HTTP status code management. |
| **Service Layer** | `AuthService`, `BookService`, `BorrowService`, `DashboardService`, `ReportService` | Transaction boundaries (`@Transactional`), inventory atomic quantity decrements/increments, overdue detection, penalty calculation. |
| **Persistence Repositories** | `UserRepository`, `BookRepository`, `BorrowRecordRepository` | Spring Data JPA interfaces executing optimized JPQL and SQL queries on MySQL. |
| **Database** | MySQL 8.x (`library_db`) | ACID relational persistence, foreign key integrity, unique email constraints. |

---

## 3. Core Business Logic & Rules

### A. Book Issue / Borrowing Workflow (7-Day Duration)
1. Verify student account exists and `active == true`.
2. Check requested book exists in `books` table.
3. Validate `available_quantity > 0`. If `0`, reject issue with `"Book is currently out of stock"`.
4. Compute `issueDate = LocalDate.now()` and `dueDate = issueDate.plusDays(7)`.
5. Persist record into `borrow_records` with `status = 'ISSUED'`.
6. Atomic stock update: `book.availableQuantity = book.availableQuantity - 1`.

### B. Overdue Detection & Late Fee Calculation (₹5 / Day)
- An active loan is overdue if `status == 'ISSUED'` and `LocalDate.now().isAfter(dueDate)`.
- Overdue Days:
  $$\text{Overdue Days} = \text{ChronoUnit.DAYS.between}(\text{dueDate}, \text{today})$$
- Estimated Penalty:
  $$\text{Estimated Penalty} = \text{Overdue Days} \times ₹5.00$$
- If returned on or before due date: $\text{Final Penalty} = ₹0.00$.
- If returned late: $\text{Final Penalty} = (\text{returnDate} - \text{dueDate}) \times ₹5.00$.

### C. Book Return Workflow
1. Locate active borrowing record by ID.
2. Check if already returned (`status == 'RETURNED'`).
3. Set `returnDate = LocalDate.now()`.
4. Calculate and record final penalty in `penalty_amount`.
5. Set `status = 'RETURNED'`.
6. Increment available stock: `book.availableQuantity = book.availableQuantity + 1`.
7. Commit changes inside a single database transaction.

---

## 4. Role-Based Access Control (RBAC) Matrix

| Endpoint | Method | Public | STUDENT | LIBRARIAN | ADMIN |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `/api/register` | `POST` | ✅ | ✅ | ✅ | ✅ |
| `/api/login` | `POST` | ✅ | ✅ | ✅ | ✅ |
| `/api/books` | `GET` | ✅ | ✅ | ✅ | ✅ |
| `/api/borrow/my` | `GET` | ❌ | ✅ | ✅ | ✅ |
| `/api/borrow` (self borrow) | `POST` | ❌ | ✅ | ✅ | ✅ |
| `/api/books` (Add/Edit/Del) | `POST/PUT/DELETE`| ❌ | ❌ | ✅ | ✅ |
| `/api/borrow/{id}/return` | `PUT` | ❌ | ❌ | ✅ | ✅ |
| `/api/borrow/overdue` | `GET` | ❌ | ❌ | ✅ | ✅ |
| `/api/reports/**` | `GET` | ❌ | ❌ | ✅ | ✅ |
| `/api/auth/students` | `GET` | ❌ | ❌ | ✅ | ✅ |
