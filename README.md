# 📚 College Library Management System
> **B.Tech Technical Training (TT) Session Project**  
> *An explainable full-stack College Library Management System built with Java 17, Spring Boot 3.x, MySQL 8.x, and Vanilla JavaScript.*

---

## 1. Abstract
The **College Library Management System** is a lightweight, responsive web application engineered to automate academic library operations. It replaces manual register logging with a centralized digital catalog, automated role-based borrowing workflows, seven-day lending limits, live overdue detection, daily penalty calculations, and administrative analytics.

---

## 2. Problem Statement
Traditional academic libraries often struggle with:
- Manual paper logs leading to misplaced books and inaccurate stock records.
- Delayed identification of overdue books and manual calculation of fines.
- Lack of role separation between students, circulation staff, and administrative leadership.
- Inefficient search mechanisms for students looking for course textbooks.

---

## 3. Project Objectives
1. **Automate Circulation:** Enable one-click book borrowing and check-ins with real-time stock updates.
2. **Transparent Lending Policy:** Automatically enforce a 7-day borrowing cycle with an automated late penalty of ₹5 per day.
3. **Role-Based Access Control (RBAC):** Provide distinct dashboards and security boundaries for Students, Librarians, and Administrators.
4. **Institutional Insights:** Deliver live executive metrics covering inventory volume, active loans, overdue items, and fine collections.
5. **Simplicity & Maintainability:** Build clean, explainable code without overengineering.

---

## 4. Technology Stack
- **Frontend:** HTML5, Modern CSS3 (Navy Blue theme), Vanilla JavaScript (ES6+ Fetch API).
- **Backend:** Java 17, Spring Boot 3.2.5, Spring Web, Spring Data JPA, Spring Security, Hibernate ORM.
- **Database:** MySQL 8.x (`library_db`).
- **Build Tool:** Apache Maven 3.9+.

---

## 5. System Architecture
```
[ Browser / HTML5 + CSS + Vanilla JS ]
                 │ (REST API / JSON via Fetch API)
                 ▼
[ Spring Boot Security Filter Chain (BCrypt, RBAC) ]
                 │
                 ▼
[ Spring REST Controllers (@RestController) ]
                 │
                 ▼
[ Service Layer (@Service, @Transactional) ]
                 │
                 ▼
[ Spring Data JPA Repositories (@Repository) ]
                 │ (JDBC / Hibernate ORM)
                 ▼
[ MySQL 8.x Relational Database (library_db) ]
```

---

## 6. Database Schema (`library_db`)
The database contains three normalized relational tables:

1. **`users`**
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `name` (VARCHAR)
   - `email` (VARCHAR UNIQUE)
   - `password_hash` (VARCHAR - BCrypt hashed)
   - `phone` (VARCHAR)
   - `role` (VARCHAR: `STUDENT`, `LIBRARIAN`, `ADMIN`)
   - `active` (BOOLEAN DEFAULT TRUE)
   - `created_at` (DATETIME)

2. **`books`**
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `title` (VARCHAR)
   - `author` (VARCHAR)
   - `category` (VARCHAR)
   - `quantity` (INT)
   - `available_quantity` (INT)

3. **`borrow_records`**
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `student_id` (BIGINT FK -> users.id)
   - `book_id` (BIGINT FK -> books.id)
   - `issue_date` (DATE)
   - `due_date` (DATE - 7 days after issue)
   - `return_date` (DATE nullable)
   - `status` (VARCHAR: `BORROWED`, `RETURNED`)
   - `penalty_amount` (DECIMAL(10,2) DEFAULT 0.00)
   - `penalty_paid` (BOOLEAN DEFAULT FALSE)

---

## 7. Main System Workflow
```
Student Registration (PUBLIC, STUDENT role only)
        ↓
Login & Authentication (Spring Security + BCrypt)
        ↓
Search & Browse Books (Live Title / Author / Category Search)
        ↓
Borrow Book (Available Stock > 0, sets 7-day Due Date)
        ↓
Database Updates (Stock decremented by 1, loan recorded)
        ↓
Return Book (Librarian verifies return, stock restored +1)
        ↓
Fine / Penalty Calculation (If returnDate > dueDate, Overdue Days × ₹5)
        ↓
Admin Dashboard Report (Live Institutional Audit & Revenue Summary)
```

---

## 8. Overdue Detection & Penalty Calculation
- **Loan Period:** 7 calendar days.
- **Daily Rate:** ₹5.00 per overdue day.
- **Formulas:**
  $$\text{Overdue Days} = \max(0, \text{Return Date / Current Date} - \text{Due Date})$$
  $$\text{Penalty Amount} = \text{Overdue Days} \times ₹5.00$$
- If returned on or before the due date, penalty is **₹0.00**.
- Duplicate returns and duplicate charges are strictly prevented.

---

## 9. Development Demo Credentials
Demo accounts are created only when development demo data is explicitly enabled with `LIBRARY_DEMO_DATA_ENABLED=true`. The seeded users below are for local demonstration only; do not enable demo data for a public or production deployment.

| Role | Email Address | Password | Default Redirect Page |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@library.com` | `Admin@123` | `admin-dashboard.html` |
| **Librarian** | `librarian@library.com` | `Lib@12345` | `librarian-dashboard.html` |
| **Student** | `student@college.com` | `Student@123` | `student-dashboard.html` |

> *Note: On the `login.html` page, click any of the 3 "Quick Demo Credentials" buttons to auto-fill these credentials instantly!*

---

## 10. How to Run the Application

### Prerequisites
1. **Java 17** installed (`java -version`).
2. **MySQL 8.x** installed and running on port 3306. Use your own local database username and password.
3. **Maven 3.9+** installed (`mvn -version`).

### Step 1: Database Setup
In MySQL Workbench, open and execute the included SQL file. From MySQL Shell, run:
```sql
SOURCE C:/path/to/CollegeLibraryManagementSystem/database/library_db.sql;
```
The script creates missing tables and adds the sample catalog only when the books table is empty. It does not drop or reset existing tables.

### Step 2: Configure Credentials (Optional)
Set database credentials as environment variables in the VS Code PowerShell terminal (replace the sample username and password locally):
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-local-mysql-password"
$env:LIBRARY_DEMO_DATA_ENABLED="true"
```
Demo data is disabled by default. Set `LIBRARY_DEMO_DATA_ENABLED=true` only for a local demonstration. Do not commit real credentials or change the default configuration to contain them.

### Step 3: Run the Spring Boot Server
From the project root, open a VS Code PowerShell terminal and run:
```bash
Set-Location .\backend
& "C:\Users\GANESH\maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```
The Maven command is a PowerShell command; if Maven is on your PATH, use `mvn spring-boot:run` instead.

### Step 4: Access the Application
Open your web browser and navigate to:
**`http://localhost:8085`**

Spring Boot serves the application pages and API from the same origin at port 8085. The public home page shows non-sensitive catalog information; library-wide loan and financial statistics require an authorized staff account.

---

## 11. REST API Endpoints Catalog

### Authentication
- `POST /api/register` - Register new student account
- `POST /api/login` - Authenticate user & start session
- `POST /api/logout` - Invalidate session
- `GET /api/auth/me` - Get current logged-in user

### Books
- `GET /api/books` - List all books / filter by `?search=...`
- `GET /api/books/{id}` - Get book details by ID
- `POST /api/books` - Add new book (Librarian/Admin)
- `PUT /api/books/{id}` - Update book (Librarian/Admin)
- `DELETE /api/books/{id}` - Delete book (Librarian/Admin)

### Borrowing & Circulation
- `POST /api/borrow` - Borrow / Issue book
- `GET /api/borrow/my` - Current student's borrowing history
- `GET /api/borrow` - All borrowing records (Librarian/Admin)
- `GET /api/borrow/overdue` - Overdue records with calculated fines (Librarian/Admin)
- `PUT /api/borrow/{id}/return` - Process return & compute fine
- `PUT /api/borrow/{id}/pay-penalty` - Mark penalty as paid

### Dashboard & Analytics
- `GET /api/dashboard` - Global KPIs (Librarian/Admin)
- `GET /api/dashboard/penalties` - Penalty metrics (Librarian/Admin)
- `GET /api/reports/borrowing` - Complete borrowing transaction log (Admin)
- `GET /api/reports/overdue` - Overdue audit statement (Admin)
- `GET /api/students` - Student list (Librarian/Admin)

### Validation and test cases
Focused unit tests cover the ₹5/day calculation for a three-day delay, return on the due date, live estimates for unreturned overdue loans, duplicate return rejection, and penalty-payment rules. Run them from `backend` with:
```powershell
& "C:\Users\GANESH\maven\apache-maven-3.9.6\bin\mvn.cmd" -Dtest=BorrowServiceTest test
```

The pre-existing `LibraryManagementApplicationTests` starts the full Spring context and therefore also requires a reachable MySQL database with valid credentials. Do not treat a unit-test pass as verification of a live MySQL-backed workflow.

### Verification results for this review
| Check | Result |
| --- | --- |
| Java 17 and Maven | Java 17.0.12 and Maven 3.9.6 were used. |
| Full Maven package | `mvn package` succeeded; 12 tests passed, 0 failed. |
| Spring Boot / MySQL startup | The packaged app started on an isolated test port and established a MySQL connection. |
| Static page and book API | Login page returned HTTP 200; the catalog API returned 10 books. |
| Registration role protection | A registration request containing an extra `ADMIN` role field still created a `STUDENT` account. |
| Role permissions | Anonymous dashboard/overdue requests returned 401; students were denied staff/admin reports; librarians could view overdue records but were denied admin reports; admins could view reports. |
| Borrow and return | Borrowing decreased available stock by one; an on-time return restored it; the final penalty was ₹0; a duplicate return returned HTTP 400. |
| Penalty unit cases | Returned three days late: ₹15; on/before due date: ₹0; an unreturned loan overdue by three days: estimated ₹15. |
| JavaScript syntax | Both shared `script.js` files passed `node --check`. |

The SQL setup file was reviewed for destructive operations and changed to be non-destructive, but it was not independently executed from a MySQL command-line client in this review environment. The app's Hibernate schema validation/updates and live repository access did succeed.

---

## 12. Faculty Demonstration Script (5-Minute Walkthrough)

When presenting to your faculty or technical evaluator, follow this structured demo script:

1. **Introduction (1 min):**
   - Open `http://localhost:8085/index.html`.
   - Point out the **Navy Blue modern UI**, book search, and explain that administrative totals are shown after staff login.
   - Explain the 3-Tier architecture (HTML/JS + Spring Boot REST APIs + MySQL 8.x).

2. **Student Workflow (1.5 min):**
   - Click **Member Login**, click **Student Demo** quick button, and click **Sign In**.
   - Show the **Student Dashboard**: active loans, notice of pre-existing overdue loans and accrued fine at ₹5/day.
   - Click **Browse Books**, search for `"Java"`, click **Borrow Book**, and confirm.
   - Show how the stock immediately decrements and the new book appears under **My Borrowings** with a 7-day due date.

3. **Librarian Circulation Workflow (1.5 min):**
   - Click **Logout**, then login with **Librarian Demo**.
   - Open **Issue & Return Desk**:
     - Demonstrate issuing another book to Rahul Sharma.
     - Switch to **Process Book Return**: select the overdue loan. Show the live modal calculating the exact penalty ($₹5 \times \text{overdue days}$).
     - Confirm return: show that stock is restored (+1) and status changes to `RETURNED`.

4. **Administrator Audit & Reports (1 min):**
   - Log in as **Admin Demo**.
   - Show **Admin Dashboard**: institutional summary, student roster, total and unpaid penalties.
   - Navigate to **Reports**: demonstrate the **All Borrowing Transactions**, **Overdue Audit**, and **Penalty Statement** tabs.
   - Click **Print / Export PDF** to show printer-friendly report styling.
