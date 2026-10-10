# 📚 College Library Management System
### B.Tech Technical Training (TT) Session Project

A modern, full-stack, enterprise-grade College Library Management System built with **Java 17, Spring Boot 3, Spring Data JPA, Spring Security, MySQL 8, and Vanilla JavaScript**.

---

## 1. Abstract
The College Library Management System is designed to automate the administrative and circulation operations of an academic library. It eliminates traditional manual register bookkeeping by providing an interactive web-based portal with role-segregated interfaces for **Students**, **Librarians**, and **Administrators**. The system streamlines textbook discovery, digital borrowing (7-day loan window), return reconciliation, real-time stock tracking, automatic overdue detection with daily penalty calculations (₹5/day), and financial audit reporting.

---

## 2. Problem Statement
Manual library management in colleges often results in:
- High human error in tracking textbook inventory and copy availability.
- Missing or disputed due dates and loan histories.
- Difficulties in detecting overdue loans and accurately computing late return fines.
- Inefficient administrative oversight and lack of consolidated circulation analytics.

---

## 3. Project Objectives
1. Provide a responsive, modern web interface with zero framework bloat.
2. Implement robust REST APIs using Java 17 and Spring Boot 3.
3. Enforce secure Role-Based Access Control (RBAC) with BCrypt password hashing and JWT authentication.
4. Maintain ACID transactional consistency in MySQL 8 for issue/return inventory adjustments.
5. Provide automatic overdue calculations and transparent penalty tracking.

---

## 4. Technology Stack

- **Frontend:** HTML5, CSS3, Vanilla JavaScript (Fetch API, Token Session Manager)
- **Backend:** Java 17, Spring Boot 3.2.4 (Spring Web, Spring Data JPA, Spring Security, JJWT)
- **Database:** MySQL 8.x (`library_db`)
- **Build Tool:** Apache Maven 3.9.x
- **Architecture:** 3-Tier Layered Monolithic REST Architecture

---

## 5. System Architecture

```
HTML5 + CSS3 + Vanilla JavaScript (11 Web Pages)
        │
        │ HTTP / JSON (Authorization: Bearer <JWT>)
        ▼
Spring Boot REST Controllers (@RestController)
        │
        ▼
Business Service Layer (@Service, @Transactional)
        │
        ▼
Spring Data JPA Repositories (@Repository)
        │
        ▼
MySQL 8.x Relational Database (library_db)
```

---

## 6. Database Schema Design (`library_db`)

The database consists of three relational tables:

```sql
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
```

---

## 7. Main Workflow

```
Student Registration / Login
        │
        ▼
Search Book in Catalog
        │
        ▼
Borrow Book (Available Stock - 1, Due Date = Today + 7 Days)
        │
        ▼
Database Updates (Transactional Persistence in MySQL)
        │
        ▼
Return Book (Available Stock + 1)
        │
        ▼
Fine / Penalty Calculation (₹5/day for Overdue Days)
        │
        ▼
Admin / Librarian Dashboard Audit Reports
```

---

## 8. User Roles & Capabilities

### 👨‍🎓 Student
- Register account and login securely.
- Search textbooks by title, author, or discipline.
- Borrow available books directly.
- View current active loans with live countdown and overdue warnings.
- View personal borrowing history and penalty dues.

### 📚 Librarian
- Full catalog management: Add new books, edit metadata, adjust quantities, delete titles.
- Issue books to registered student accounts with 7-day loan terms.
- Process book returns with automatic penalty calculation.
- Monitor overdue loans and view financial fine summaries.

### ⚙️ Administrator
- High-level system overview: Total titles, total copies, available copies, active loans, returned books, overdue loans, registered students, and financial penalty metrics.
- Audit registered student accounts.
- Generate and print comprehensive circulation, overdue, and penalty reports.

---

## 9. Overdue Detection & Penalty Calculation Rules

- **Loan Duration:** 7 Days.
- **Daily Late Rate:** ₹5.00 per day.
- **Overdue Check:**
  $$\text{Overdue Days} = \text{currentDate} - \text{dueDate} \quad (\text{if } \text{status} = \text{'ISSUED'} \land \text{currentDate} > \text{dueDate})$$
  $$\text{Estimated Penalty} = \text{Overdue Days} \times ₹5.00$$
- **On Return:**
  - If returned on or before due date: $\text{Final Penalty} = ₹0.00$.
  - If returned late: $\text{Final Penalty} = (\text{returnDate} - \text{dueDate}) \times ₹5.00$.

---

## 10. Development Demo Credentials

| Role | Email Address | Password |
| :--- | :--- | :--- |
| **Student** | `student@college.com` | `Student@123` |
| **Librarian** | `librarian@library.com` | `Lib@12345` |
| **Administrator** | `admin@library.com` | `Admin@123` |

*(The login page includes 1-click quick fill buttons for easy testing during the evaluation).*

---

## 11. How to Run the Project

### Step 1: Ensure MySQL is Running
Verify MySQL 8 is running and create the database (if not already created):
```sql
CREATE DATABASE IF NOT EXISTS library_db;
```

### Step 2: Configure Database Credentials
Check `backend/src/main/resources/application.properties` (or set environment variables `DB_USERNAME` and `DB_PASSWORD`):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

### Step 3: Run the Spring Boot Application
From the workspace root directory:
```powershell
# Using the embedded Maven:
& "tools\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run -f "backend\pom.xml"
```
Or using standard Maven:
```bash
cd backend
mvn spring-boot:run
```

### Step 4: Open the Application in Browser
Open your browser and navigate to:
```
http://localhost:8080
```
*(All 11 frontend pages are bundled and served directly by Spring Boot without needing any external server or Node.js)*.

---

## 12. Project Folder Structure

```
CollegeLibraryManagementSystem/
├── frontend/
│   ├── index.html
│   ├── register.html
│   ├── login.html
│   ├── student-dashboard.html
│   ├── books.html
│   ├── my-borrowings.html
│   ├── librarian-dashboard.html
│   ├── manage-books.html
│   ├── issue-return.html
│   ├── admin-dashboard.html
│   ├── reports.html
│   ├── css/
│   │   └── style.css
│   └── js/
│       └── script.js
├── backend/
│   ├── pom.xml
│   ├── application.properties.example
│   └── src/
│       └── main/
│           ├── java/com/college/library/
│           │   ├── LibraryApplication.java
│           │   ├── config/
│           │   ├── controller/
│           │   ├── dto/
│           │   ├── entity/
│           │   ├── repository/
│           │   ├── security/
│           │   └── service/
│           └── resources/
│               ├── application.properties
│               ├── application.properties.example
│               └── static/ (11 HTML pages, css, js)
├── database/
│   └── library_db.sql
├── docs/
│   ├── ARCHITECTURE.md
│   └── VIVA_QUESTIONS.md
└── README.md
```

---

## 13. Step-by-Step Faculty Demonstration Script

1. **Homepage (`http://localhost:8080`):** Showcase the live summary metrics (Total titles, Available copies, Active loans, Overdue loans) and featured catalog.
2. **Student Self-Service:**
   - Sign in as Student (`student@college.com` / `Student@123`).
   - Show Student Dashboard with active loans and overdue fine breakdown.
   - Go to Book Catalog (`books.html`), filter by discipline, and borrow a book. Observe available copies decreasing by 1.
   - Check `My Borrowings` (`my-borrowings.html`) showing the newly issued loan and calculated due date (7 days).
3. **Librarian Circulation Desk:**
   - Sign in as Librarian (`librarian@library.com` / `Lib@12345`).
   - Open Issue / Return desk (`issue-return.html`).
   - Process return on an overdue book. Demonstrate the late fee calculation alert (`₹5/day`).
   - Confirm return and observe available stock automatically incrementing by 1 in MySQL.
4. **Administrator Audit & Analytics:**
   - Sign in as Admin (`admin@library.com` / `Admin@123`).
   - Show Admin Dashboard metrics and registered student roster.
   - Open Audit Reports (`reports.html`), switch between Master Circulation, Overdue Loans, and Penalty Audit tabs, and demonstrate the print view.
