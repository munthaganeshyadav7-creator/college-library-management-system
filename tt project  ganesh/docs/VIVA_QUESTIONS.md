# College Library Management System - Viva Questions & Answers
### B.Tech Technical Training (TT) Comprehensive Faculty Evaluation Guide

---

### Q1: What is the main objective of this College Library Management System?
**Answer:** The objective is to automate academic library operations including book discovery, student self-service and circulation desk borrowing, return tracking, automatic overdue detection, daily late fee (penalty) calculation, real-time inventory management, and audit reporting for students, librarians, and administrators.

---

### Q2: What is the technology stack used in this project and why?
**Answer:**
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (Fetch API). Lightweight, fast, requiring no build steps or heavy node frameworks.
- **Backend:** Java 17 and Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security). Provides robust, enterprise-grade REST APIs, dependency injection, and automatic ORM persistence.
- **Database:** MySQL 8.x for reliable relational ACID data storage with foreign key constraints.
- **Build Tool:** Apache Maven for clean dependency management and automated builds.

---

### Q3: Explain the 3-tier architecture of this application.
**Answer:**
1. **Presentation Tier (Frontend):** Modern responsive HTML5/CSS3 pages interacting with the backend via asynchronous `fetch()` calls.
2. **Application / Business Tier (Spring Boot):**
   - **Controllers:** Expose REST API endpoints and handle HTTP requests/responses.
   - **Service Layer:** Houses core business logic (7-day loans, ₹5/day penalty calculation, atomic inventory stock adjustments).
   - **Security Layer:** Enforces JWT token validation and role-based access control (RBAC).
   - **Data Access Layer:** Spring Data JPA Repositories converting Java entities into SQL queries.
3. **Database Tier (MySQL):** Relational tables (`users`, `books`, `borrow_records`) storing persistent library data.

---

### Q4: How is user authentication and authorization handled?
**Answer:**
We use **Spring Security** with **JSON Web Tokens (JWT)** and **BCrypt Password Hashing**:
1. When a user logs in with email and password at `/api/login`, the `AuthenticationManager` verifies the credentials against the BCrypt hash in MySQL.
2. Upon success, a stateless signed JWT token is generated containing user ID, email, and role.
3. The frontend stores this token in browser `localStorage` and attaches it to the `Authorization: Bearer <token>` header for subsequent requests.
4. `JwtAuthenticationFilter` intercepts each request, validates the token signature and expiration, and sets the Spring Security context with role permissions (`ROLE_STUDENT`, `ROLE_LIBRARIAN`, `ROLE_ADMIN`).

---

### Q5: Why did you use BCrypt password hashing instead of storing plain text?
**Answer:** Plaintext passwords create severe security vulnerabilities if the database is accessed. BCrypt uses a salted cryptographic one-way hashing algorithm with an adjustable work factor, making rainbow table and brute-force attacks computationally infeasible.

---

### Q6: How does the book borrowing workflow work in the backend?
**Answer:**
In `BorrowService.java`:
1. Verifies the student account exists and is active.
2. Checks that the book exists and `availableQuantity > 0` (preventing over-borrowing).
3. Sets `issueDate = LocalDate.now()` and `dueDate = issueDate.plusDays(7)`.
4. Saves a new `BorrowRecord` in MySQL with `status = 'ISSUED'`.
5. Decrements `availableQuantity` by 1 atomically within a database transaction.

---

### Q7: How is overdue detection and late fee / penalty calculated?
**Answer:**
- **Daily Rate:** ₹5.00 per day.
- **Overdue Check:** If a book is in `ISSUED` status and `currentDate.isAfter(dueDate)`:
  $$\text{Overdue Days} = \text{currentDate} - \text{dueDate}$$
  $$\text{Estimated Penalty} = \text{Overdue Days} \times ₹5.00$$
- **On Return:**
  - If returned on or before due date: $\text{Final Penalty} = ₹0.00$.
  - If returned late: $\text{Final Penalty} = (\text{returnDate} - \text{dueDate}) \times ₹5.00$.
  - Status is updated to `RETURNED`, `penalty_amount` is saved, and available stock is incremented by 1.

---

### Q8: What happens during book return to maintain data consistency?
**Answer:**
The `returnBook()` method is annotated with `@Transactional`. This ensures that updating the borrowing record status to `RETURNED`, computing the final penalty amount, and incrementing `book.availableQuantity` by 1 either all succeed together or rollback completely in case of any database error.

---

### Q9: How are roles segregated in the frontend and backend?
**Answer:**
- **Backend (Enforcement):** Configured via `@PreAuthorize("hasAnyRole(...)")` and `SecurityConfig.java`. For example, only `LIBRARIAN` and `ADMIN` can access `/api/books` POST/PUT/DELETE and `/api/borrow/{id}/return`.
- **Frontend (UX):** `script.js` checks the stored role and conditionally renders navigation links and routes users to `student-dashboard.html`, `librarian-dashboard.html`, or `admin-dashboard.html`.

---

### Q10: What is Spring Data JPA and what advantages does it offer?
**Answer:** Spring Data JPA is an abstraction layer built on top of Hibernate (JPA provider). It reduces boilerplate DAO/JDBC code by providing automatic query generation from repository method names (e.g., `findByEmail`, `findByCategoryIgnoreCase`) and supports custom JPQL/SQL `@Query` annotations.

---

### Q11: What tables exist in `library_db` and how are they related?
**Answer:**
1. `users`: Stores user credentials, contact details, and role (`STUDENT`, `LIBRARIAN`, `ADMIN`).
2. `books`: Stores catalog metadata (`title`, `author`, `category`, `quantity`, `available_quantity`).
3. `borrow_records`: Stores transaction details (`issue_date`, `due_date`, `return_date`, `status`, `penalty_amount`, `penalty_paid`).
   - Foreign keys: `student_id -> users(id)` and `book_id -> books(id)`.

---

### Q12: How does the frontend communicate with the backend?
**Answer:** The frontend uses standard asynchronous JavaScript `fetch()` API calls with JSON payloads:
```javascript
const response = await fetch(`${API_BASE_URL}/books`, {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
});
const data = await response.json();
```

---

### Q13: How does the system prevent duplicate book returns?
**Answer:** In `BorrowService.java`, before processing a return, it checks if `record.getStatus().equals("RETURNED")`. If true, it throws an `IllegalStateException` with the message `"This book has already been returned on <date>"`, preventing duplicate inventory increments and duplicate fine updates.

---

### Q14: How does the system handle book deletion if copies are currently borrowed?
**Answer:** In `BookService.java`, the system calculates `borrowedCopies = book.getQuantity() - book.getAvailableQuantity()`. If `borrowedCopies > 0`, deletion is rejected with an error informing the librarian to process the active student returns first.

---

### Q15: How are demo accounts initialized on application startup?
**Answer:** A Spring component named `DataInitializer` implements `CommandLineRunner`. When the Spring Boot application boots, `run()` executes, checks if demo users and books exist in MySQL, and automatically populates default demo accounts with BCrypt-hashed passwords and core academic textbooks.

---

### Q16: What are the development demo credentials?
**Answer:**
- **Student:** `student@college.com` / `Student@123`
- **Librarian:** `librarian@library.com` / `Lib@12345`
- **Administrator:** `admin@library.com` / `Admin@123`
