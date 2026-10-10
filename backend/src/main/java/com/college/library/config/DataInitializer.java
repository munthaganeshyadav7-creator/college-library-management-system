package com.college.library.config;

import com.college.library.entity.Book;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Initializes the default demo accounts and initial catalog data if not already present.
 */
@Component
@ConditionalOnProperty(name = "library.demo-data.enabled", havingValue = "true")
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
    @Transactional
    public void run(String... args) {
        // 1. Initialize Demo Users
        initUser("Admin User", "admin@library.com", "Admin@123", "9876543210", Role.ADMIN);
        initUser("Chief Librarian", "librarian@library.com", "Lib@12345", "9876543211", Role.LIBRARIAN);
        User demoStudent = initUser("Rahul Sharma", "student@college.com", "Student@123", "9876543212", Role.STUDENT);

        // Add only missing sample titles so existing books and inventory are preserved.
        List<Book> sampleBooks = List.of(
            new Book("Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", 5, 5),
            new Book("Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Software Engineering", 4, 4),
            new Book("Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma", "Software Engineering", 3, 3),
            new Book("Operating System Concepts", "Abraham Silberschatz", "Computer Science", 4, 4),
            new Book("Database System Concepts", "Abraham Silberschatz", "Database Management", 6, 6),
            new Book("Artificial Intelligence: A Modern Approach", "Stuart Russell", "Artificial Intelligence", 3, 3),
            new Book("Computer Networking: A Top-Down Approach", "James Kurose", "Networking", 5, 5),
            new Book("Java: The Complete Reference", "Herbert Schildt", "Programming", 7, 7),
            new Book("Python Crash Course", "Eric Matthes", "Programming", 4, 4),
            new Book("Cloud Computing: Principles and Paradigms", "Rajkumar Buyya", "Cloud Computing", 3, 3)
        );
        for (Book sampleBook : sampleBooks) {
            if (!bookRepository.existsByTitleIgnoreCase(sampleBook.getTitle())) {
                bookRepository.save(sampleBook);
            }
        }

        // 3. Initialize Demo Borrow Records if empty (to showcase overdue detection and penalty)
        if (borrowRecordRepository.count() == 0 && demoStudent != null) {
            List<Book> books = bookRepository.findAll();
            if (books.size() >= 3) {
                // Active loan (on-time)
                Book b1 = books.get(0);
                if (b1.getAvailableQuantity() > 0) {
                    b1.setAvailableQuantity(b1.getAvailableQuantity() - 1);
                    bookRepository.save(b1);
                    BorrowRecord r1 = new BorrowRecord(demoStudent, b1, LocalDate.now().minusDays(2), LocalDate.now().plusDays(5));
                    borrowRecordRepository.save(r1);
                }

                // Overdue loan (issued 12 days ago, due 5 days ago, overdue by 5 days -> penalty ₹25)
                Book b2 = books.get(1);
                if (b2.getAvailableQuantity() > 0) {
                    b2.setAvailableQuantity(b2.getAvailableQuantity() - 1);
                    bookRepository.save(b2);
                    BorrowRecord r2 = new BorrowRecord(demoStudent, b2, LocalDate.now().minusDays(12), LocalDate.now().minusDays(5));
                    borrowRecordRepository.save(r2);
                }

                // Returned book with paid penalty (returned late 2 days late, penalty ₹10 paid)
                Book b3 = books.get(2);
                BorrowRecord r3 = new BorrowRecord(demoStudent, b3, LocalDate.now().minusDays(20), LocalDate.now().minusDays(13));
                r3.setReturnDate(LocalDate.now().minusDays(11));
                r3.setStatus("RETURNED");
                r3.setPenaltyAmount(10.0);
                r3.setPenaltyPaid(true);
                borrowRecordRepository.save(r3);
            }
        }

        System.out.println(">>> Demo accounts verified and seed records loaded successfully.");
    }

    private User initUser(String name, String email, String rawPassword, String phone, Role role) {
        String cleanEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(cleanEmail).orElseGet(() -> {
            User newUser = new User(name, cleanEmail, passwordEncoder.encode(rawPassword), phone, role);
            return userRepository.save(newUser);
        });
    }
}
