# College Library Management System - Faculty Viva Questions & Answers
**B.Tech Technical Training (TT) Session Project**

---

### Q1. What is the architecture of this College Library Management System?
**Answer:**  
The project uses a **3-Tier / Layered Client-Server Architecture**:
1. **Frontend (Presentation Tier):** Built with HTML5, CSS3, and Vanilla JavaScript. It makes asynchronous HTTP requests using the JavaScript `fetch()` API.
2. **Backend (Application / Business Tier):** Built using Java 17 and Spring Boot 3.x, structured into REST Controllers, Service Layer, and Spring Security.
3. **Database (Data Tier):** MySQL 8.x relational database accessed via Spring Data JPA and Hibernate ORM.

---

### Q2. How do the Frontend, Backend, and Database communicate with each other?
**Answer:**  
- When a user performs an action on a web page (e.g., clicks "Borrow Book"), Vanilla JavaScript captures the event and sends an HTTP request (`POST /api/borrow`) with JSON payload using the `fetch()` API.
- Spring Boot's `@RestController` receives the request, validates the data, and invokes the appropriate `@Service` method.
- The service executes business logic (e.g., checks stock availability) and uses Spring Data JPA repositories to perform SQL operations on the MySQL database via JDBC.
- The database returns the updated records to JPA, which are converted to DTOs and returned to the browser as JSON responses (`ApiResponse<T>`).
- The frontend dynamically updates the DOM without reloading the full page.

---

### Q3. Why did you use Spring Boot instead of standard Spring or Java Servlets?
**Answer:**  
- **Auto-configuration:** Spring Boot automatically configures beans based on dependencies in `pom.xml`.
- **Embedded Tomcat Server:** We do not need an external server like standalone Apache Tomcat or WAR file deployment; running the `main()` method starts the embedded server on port 8085.
- **Starter Dependencies:** Simple starters like `spring-boot-starter-web` and `spring-boot-starter-data-jpa` reduce boilerplate XML configuration.
- **Production-ready features:** Built-in validation, exception handling, and metrics.

---

### Q4. What is Spring Data JPA, and how does it simplify database operations?
**Answer:**  
Spring Data JPA is an abstraction built on top of JPA (Java Persistence API) and Hibernate. Instead of writing low-level JDBC code (`Connection`, `PreparedStatement`, `ResultSet`), we declare repository interfaces extending `JpaRepository<Entity, ID>`. Spring automatically generates CRUD implementation methods like `save()`, `findById()`, `findAll()`, and `deleteById()`. We can also write JPQL queries using the `@Query` annotation.

---

### Q5. What is the role of the `@Entity` and `@Table` annotations?
**Answer:**  
- `@Entity` marks a Java POJO class as a persistent entity representing a relational table.
- `@Table(name = "...")` specifies the exact table name in the MySQL database (e.g., `@Table(name = "books")`).
- Each instance variable corresponds to a column in the database, with `@Id` specifying the primary key and `@GeneratedValue` specifying auto-increment strategy.

---

### Q6. How is user authentication and authorization handled?
**Answer:**  
- **Authentication:** Spring Security validates user credentials against the `users` table via `CustomUserDetailsService` implementing `loadUserByUsername()`.
- **Password Security:** Passwords are never stored in plaintext. They are encrypted using `BCryptPasswordEncoder` with a 10-round salt hash.
- **Authorization:** Role-Based Access Control (RBAC). We assign roles (`ROLE_STUDENT`, `ROLE_LIBRARIAN`, `ROLE_ADMIN`). In `SecurityConfig`, endpoints like `/api/books` (POST/PUT/DELETE) are restricted using `.hasAnyRole("LIBRARIAN", "ADMIN")`, while `/api/borrow/my` is restricted to `ROLE_STUDENT`.

---

### Q7. What are the key business rules for book borrowing and returning in your project?
**Answer:**  
1. **Borrowing:**
   - Verifies student account is active.
   - Checks that the book exists and `available_quantity > 0`.
   - Records issue date (today) and automatically sets due date to **7 days** later (`issueDate.plusDays(7)`).
   - Decrements `available_quantity` by 1.
2. **Returning:**
   - Checks that the book is currently marked as `BORROWED` (prevents duplicate returns).
   - Records actual `return_date`.
   - Increments `available_quantity` by 1.
   - Updates status to `RETURNED`.

---

### Q8. How is the overdue penalty calculated?
**Answer:**  
- A loan is overdue if it is not returned and today's date is past the `due_date`.
- **Penalty Rate:** ₹5 per overdue day.
- **Formula:**
  $$\text{Overdue Days} = \text{Date of Return / Current Date} - \text{Due Date}$$
  $$\text{Penalty Amount} = \text{Overdue Days} \times 5.0$$
- If returned on or before the due date, penalty is ₹0.
- For example, if due on Oct 27 and returned on Oct 30, overdue days = 3, and penalty = $3 \times 5 = ₹15$.

---

### Q9. Why is `@Transactional` used on Service methods?
**Answer:**  
`@Transactional` guarantees the **ACID** (Atomicity, Consistency, Isolation, Durability) properties of database operations:
- For example, when returning a book, two operations occur: updating the `borrow_records` table and incrementing `available_quantity` in `books`.
- If one operation fails, `@Transactional` rolls back both operations, preventing data inconsistency.

---

### Q10. What is a DTO (Data Transfer Object), and why is it used?
**Answer:**  
A DTO (e.g., `BookDto`, `BorrowRecordDto`, `LoginRequest`) is an object designed to carry data between processes (between frontend JSON and backend).  
**Benefits:**
- Decouples domain entities from REST API payloads.
- Prevents over-posting attacks and circular reference issues during JSON serialization.
- Allows sending calculated fields (such as `overdueDays` and `estimatedPenalty`) without creating unnecessary database columns.

---

### Q11. Explain the purpose of each important file in the project.
**Answer:**
- **`pom.xml`:** Maven build configuration file declaring Java 17, Spring Boot dependencies, and plugins.
- **`application.properties`:** Configures server port (8085), MySQL connection parameters, and Hibernate dialect.
- **`DataInitializer.java`:** Runs on application startup to seed demo accounts and catalog books if the database is clean.
- **`SecurityConfig.java`:** Defines the security filter chain, permitted routes, role restrictions, and BCrypt bean.
- **`BorrowService.java`:** Contains core business logic for issuing books, calculating overdue fines, and processing returns.
- **`style.css`:** Shared responsive stylesheet implementing modern Navy Blue design, cards, and UI components.
- **`script.js`:** Shared Vanilla JS client handling API calls, authentication sessions, and dynamic DOM rendering.

---

### Q12. What are the three user roles and their capabilities?
**Answer:**  
1. **Student:** Can register an account, browse/search books, borrow available copies, check due dates, and monitor personal fines.
2. **Librarian:** Can manage inventory (add, edit, delete books), operate circulation desk (issue books to students, process returns, calculate late fines).
3. **Administrator:** Has top-level oversight: views institutional KPIs (total books, active loans, overdue count, collected & pending penalties), student roster, and audit reports.

---

### Q13. Why did you choose Vanilla JavaScript instead of React or Angular?
**Answer:**  
- **Requirement Alignment:** The project is designed for a B.Tech Technical Training evaluation where clarity and core programming concepts matter most.
- **Simplicity:** No heavy build tools (Webpack, Vite, npm, Babel) or complex state managers.
- **Fast Execution:** Static HTML/JS loads instantly directly from Spring Boot's static folder or any web browser with zero overhead.

---

### Q14. What HTTP status codes are returned by your REST APIs?
**Answer:**  
- `200 OK`: Successful retrieval, update, or deletion.
- `201 Created`: Successful creation (e.g., student registration, book added, loan created).
- `400 Bad Request`: Validation failure or business rule violation (e.g., borrowing when stock is 0).
- `401 Unauthorized`: Missing or invalid credentials.
- `403 Forbidden`: Authenticated user lacks permission for that endpoint (e.g., Student trying to delete a book).
- `404 Not Found`: Resource does not exist (e.g., book ID not found).

---

### Q15. How do you prevent duplicate returns or negative stock in MySQL?
**Answer:**  
The service locks the loan row before processing a return, then locks the book row while updating stock. It rejects a loan already marked returned and prevents stock from exceeding total inventory. Borrowing also locks the book row before checking available copies.

---

### Q16. Why are the staff dashboard and reports protected?
**Answer:**  
They contain library-wide student, circulation, and penalty information. Students can see their own borrowing history, while librarian and administrator accounts see staff functions. Administrative reports are restricted to the administrator.

### Q17. How does the system prevent two users from borrowing the last copy at the same time?
**Answer:**  
The borrow operation runs in a database transaction and locks the selected book row for writing before checking its available quantity. A competing request must wait for that transaction and then sees the updated stock.

### Q18. Why is the database setup script safe to run again?
**Answer:**  
It uses `CREATE TABLE IF NOT EXISTS` and adds starter books only when the book table is empty. It does not drop tables, so rerunning setup does not erase existing loans or accounts.

### Q19. How are demo users created?
**Answer:**  
Demo data is disabled by default. For a local presentation I set `LIBRARY_DEMO_DATA_ENABLED=true` in the terminal before starting Spring Boot. The initializer uses BCrypt to hash the demo passwords.

### Q20. How does the project calculate a live penalty for a book that is still out?
**Answer:**  
It compares the due date with `LocalDate.now()`. When today is later than the due date, it multiplies the overdue days by ₹5. The estimate is returned by the API and is not saved as the final penalty until the book is returned.

### Q21. What should I verify before presenting the project?
**Answer:**  
Start MySQL, run the schema script, set local database credentials, enable demo data for the session, and start the app on port 8085. Then test one login per role, search a book, borrow it, return it, and confirm the stock and penalty report.
