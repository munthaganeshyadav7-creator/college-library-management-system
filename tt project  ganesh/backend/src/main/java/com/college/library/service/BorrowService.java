package com.college.library.service;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.dto.BorrowRequest;
import com.college.library.entity.Book;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrating book issue, return, overdue detection, and penalty calculation.
 */
@Service
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Value("${library.penalty.daily-rate:5.00}")
    private double dailyPenaltyRate;

    @Value("${library.loan.duration-days:7}")
    private int loanDurationDays;

    public BorrowService(BorrowRecordRepository borrowRecordRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Borrow a book for a student.
     * Section 8 Workflow Implementation.
     */
    @Transactional
    public BorrowRecordDto borrowBook(BorrowRequest request, String authenticatedEmail) {
        // 1. Resolve student (either by studentId, studentEmail, or current authenticated student)
        User student;
        if (request.getStudentId() != null) {
            student = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + request.getStudentId()));
        } else if (request.getStudentEmail() != null && !request.getStudentEmail().trim().isEmpty()) {
            student = userRepository.findByEmail(request.getStudentEmail().trim().toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found with email: " + request.getStudentEmail()));
        } else {
            student = userRepository.findByEmail(authenticatedEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        }

        if (!student.isActive()) {
            throw new IllegalStateException("Student account is inactive. Cannot issue books.");
        }

        // 2. Check that the book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + request.getBookId()));

        // 3. Check that available_quantity is greater than zero
        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' is currently out of stock (0 available copies).");
        }

        // 4. Set issue date & 5. Set due date (7 days later)
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(loanDurationDays);

        // 6. Save the borrowing record in MySQL
        BorrowRecord record = new BorrowRecord(student, book, issueDate, dueDate);
        record.setStatus("ISSUED");
        record.setPenaltyAmount(0.0);
        record.setPenaltyPaid(false);

        // 7. Decrease available_quantity by 1
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        return mapToDto(savedRecord);
    }

    /**
     * Process book return and compute final late return penalty.
     * Section 10 Workflow Implementation.
     */
    @Transactional
    public BorrowRecordDto returnBook(Long recordId) {
        // 1. Find active borrowing record
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with ID: " + recordId));

        // 2. Check that the book has not already been returned
        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("This book has already been returned on " + record.getReturnDate());
        }

        // 3. Record actual return date
        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);

        // 4. Calculate final penalty
        if (returnDate.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            double penalty = overdueDays * dailyPenaltyRate;
            record.setPenaltyAmount(penalty);
            record.setPenaltyPaid(false); // Pending payment if penalty > 0
        } else {
            record.setPenaltyAmount(0.0);
            record.setPenaltyPaid(true); // No penalty, marked as cleared
        }

        // 5. Update borrowing status to RETURNED
        record.setStatus("RETURNED");

        // 6. Increase available_quantity by 1
        Book book = record.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        // 7. Save all changes in database transaction
        BorrowRecord updatedRecord = borrowRecordRepository.save(record);

        // 8. Return confirmation DTO
        return mapToDto(updatedRecord);
    }

    /**
     * Mark penalty as paid.
     */
    @Transactional
    public BorrowRecordDto payPenalty(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with ID: " + recordId));

        if (record.getPenaltyAmount() <= 0) {
            record.setPenaltyPaid(true);
        } else {
            record.setPenaltyPaid(true);
        }

        BorrowRecord updated = borrowRecordRepository.save(record);
        return mapToDto(updated);
    }

    /**
     * Get borrowing history for current student.
     */
    public List<BorrowRecordDto> getStudentBorrowings(String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return borrowRecordRepository.findByStudentOrderByIssueDateDesc(student)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all borrowing records (Librarian / Admin).
     */
    public List<BorrowRecordDto> getAllBorrowings() {
        return borrowRecordRepository.findAllByOrderByIssueDateDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get list of overdue borrowings.
     */
    public List<BorrowRecordDto> getOverdueBorrowings() {
        LocalDate today = LocalDate.now();
        return borrowRecordRepository.findOverdueIssuedRecords(today)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to DTO with dynamic overdue and penalty calculation.
     * Section 9 Business Logic.
     */
    public BorrowRecordDto mapToDto(BorrowRecord record) {
        BorrowRecordDto dto = new BorrowRecordDto();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudent().getId());
        dto.setStudentName(record.getStudent().getName());
        dto.setStudentEmail(record.getStudent().getEmail());
        dto.setBookId(record.getBook().getId());
        dto.setBookTitle(record.getBook().getTitle());
        dto.setBookAuthor(record.getBook().getAuthor());
        dto.setBookCategory(record.getBook().getCategory());
        dto.setIssueDate(record.getIssueDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setPenaltyPaid(record.isPenaltyPaid());

        LocalDate today = LocalDate.now();

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            dto.setStatus("RETURNED");
            dto.setPenaltyAmount(record.getPenaltyAmount());
            dto.setEstimatedPenalty(record.getPenaltyAmount());
            if (record.getReturnDate() != null && record.getReturnDate().isAfter(record.getDueDate())) {
                dto.setOverdueDays(ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate()));
            } else {
                dto.setOverdueDays(0);
            }
        } else {
            // Still ISSUED
            if (today.isAfter(record.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
                double estimatedPenalty = overdueDays * dailyPenaltyRate;
                dto.setStatus("OVERDUE");
                dto.setOverdueDays(overdueDays);
                dto.setEstimatedPenalty(estimatedPenalty);
                dto.setPenaltyAmount(estimatedPenalty);
            } else {
                dto.setStatus("ISSUED");
                dto.setOverdueDays(0);
                dto.setEstimatedPenalty(0.0);
                dto.setPenaltyAmount(0.0);
            }
        }

        return dto;
    }
}
