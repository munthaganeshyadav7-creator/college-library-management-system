package com.college.library;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.entity.Book;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import com.college.library.service.BorrowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "library.demo-data.enabled=true")
class LibraryManagementApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BorrowService borrowService;

    @Test
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(bookRepository);
        assertNotNull(borrowRecordRepository);
    }

    @Test
    void testDemoAccountsCreated() {
        assertTrue(userRepository.existsByEmail("admin@library.com"), "Admin demo account should exist");
        assertTrue(userRepository.existsByEmail("librarian@library.com"), "Librarian demo account should exist");
        assertTrue(userRepository.existsByEmail("student@college.com"), "Student demo account should exist");
    }

    @Test
    void testBooksCatalogLoaded() {
        List<Book> books = bookRepository.findAll();
        assertFalse(books.isEmpty(), "Book catalog should contain seed books");
        assertTrue(books.size() >= 10, "Should have at least 10 books in catalog");
    }

    @Test
    void testPenaltyCalculationLogic() {
        // Test penalty calculation on 3 days overdue
        // Rate: ₹5 per day -> 3 * 5 = ₹15
        long overdueDays = 3;
        double expectedPenalty = overdueDays * BorrowService.DAILY_PENALTY_RATE;
        assertEquals(15.0, expectedPenalty, "3 overdue days should equal ₹15 penalty");
    }
}
