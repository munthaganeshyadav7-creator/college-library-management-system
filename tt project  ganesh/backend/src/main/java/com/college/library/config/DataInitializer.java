package com.college.library.config;

import com.college.library.entity.Book;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlled development database initialization.
 * Automatically provisions demo user accounts and standard book inventory on first run.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BookRepository bookRepository,
                           BorrowRecordRepository borrowRecordRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
        seedSampleBorrowings();
    }

    private void seedUsers() {
        // 1. Admin Account
        if (!userRepository.existsByEmail("admin@library.com")) {
            User admin = new User(
                    "System Administrator",
                    "admin@library.com",
                    passwordEncoder.encode("Admin@123"),
                    "9876543210",
                    Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println(">> Initialized Demo Admin: admin@library.com");
        }

        // 2. Librarian Account
        if (!userRepository.existsByEmail("librarian@library.com")) {
            User librarian = new User(
                    "Head Librarian",
                    "librarian@library.com",
                    passwordEncoder.encode("Lib@12345"),
                    "9876543211",
                    Role.LIBRARIAN
            );
            userRepository.save(librarian);
            System.out.println(">> Initialized Demo Librarian: librarian@library.com");
        }

        // 3. Student Account
        if (!userRepository.existsByEmail("student@college.com")) {
            User student = new User(
                    "Rahul Sharma",
                    "student@college.com",
                    passwordEncoder.encode("Student@123"),
                    "9876543212",
                    Role.STUDENT
            );
            userRepository.save(student);
            System.out.println(">> Initialized Demo Student: student@college.com");
        }

        // Additional student for testing
        if (!userRepository.existsByEmail("priya@college.com")) {
            User student2 = new User(
                    "Priya Patel",
                    "priya@college.com",
                    passwordEncoder.encode("Student@123"),
                    "9876543213",
                    Role.STUDENT
            );
            userRepository.save(student2);
        }
    }

    private void seedBooks() {
        if (bookRepository.count() == 0) {
            List<Book> initialBooks = List.of(
                    new Book("Effective Java (3rd Edition)", "Joshua Bloch", "Computer Science", 10, 10),
                    new Book("Introduction to Algorithms (CLRS)", "Thomas H. Cormen", "Computer Science", 8, 8),
                    new Book("Database System Concepts", "Abraham Silberschatz", "Information Technology", 12, 12),
                    new Book("Operating System Concepts", "Abraham Silberschatz", "Computer Science", 6, 6),
                    new Book("Computer Networking: A Top-Down Approach", "James Kurose", "Networks", 7, 7),
                    new Book("Clean Code: Agile Software Craftsmanship", "Robert C. Martin", "Software Engineering", 5, 5),
                    new Book("Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig", "AI & ML", 9, 9),
                    new Book("Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma (GoF)", "Software Engineering", 4, 4),
                    new Book("Discrete Mathematics and Its Applications", "Kenneth H. Rosen", "Mathematics", 15, 15),
                    new Book("Head First Java", "Kathy Sierra, Bert Bates", "Programming", 6, 6)
            );
            bookRepository.saveAll(initialBooks);
            System.out.println(">> Initialized Catalog with " + initialBooks.size() + " academic textbooks.");
        }
    }

    private void seedSampleBorrowings() {
        if (borrowRecordRepository.count() == 0) {
            User student = userRepository.findByEmail("student@college.com").orElse(null);
            User student2 = userRepository.findByEmail("priya@college.com").orElse(null);
            List<Book> books = bookRepository.findAll();

            if (student != null && books.size() >= 3) {
                // Active on-time loan
                Book book1 = books.get(0);
                if (book1.getAvailableQuantity() > 0) {
                    BorrowRecord activeLoan = new BorrowRecord(
                            student,
                            book1,
                            LocalDate.now().minusDays(2),
                            LocalDate.now().plusDays(5)
                    );
                    book1.setAvailableQuantity(book1.getAvailableQuantity() - 1);
                    bookRepository.save(book1);
                    borrowRecordRepository.save(activeLoan);
                }

                // Sample Overdue loan (issued 10 days ago, due 3 days ago => 3 overdue days => Rs. 15 penalty)
                Book book2 = books.get(1);
                if (book2.getAvailableQuantity() > 0) {
                    BorrowRecord overdueLoan = new BorrowRecord(
                            student,
                            book2,
                            LocalDate.now().minusDays(10),
                            LocalDate.now().minusDays(3)
                    );
                    book2.setAvailableQuantity(book2.getAvailableQuantity() - 1);
                    bookRepository.save(book2);
                    borrowRecordRepository.save(overdueLoan);
                }

                // Sample Returned loan (returned on time)
                Book book3 = books.get(2);
                BorrowRecord returnedLoan = new BorrowRecord(
                        student2 != null ? student2 : student,
                        book3,
                        LocalDate.now().minusDays(20),
                        LocalDate.now().minusDays(13)
                );
                returnedLoan.setReturnDate(LocalDate.now().minusDays(14));
                returnedLoan.setStatus("RETURNED");
                returnedLoan.setPenaltyAmount(0.0);
                returnedLoan.setPenaltyPaid(true);
                borrowRecordRepository.save(returnedLoan);

                System.out.println(">> Initialized Sample Borrowing records (Active, Overdue, and Returned).");
            }
        }
    }
}
