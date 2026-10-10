package com.college.library.service;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.entity.Book;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowService {

    public static final double DAILY_PENALTY_RATE = 5.0; // ₹5 per overdue day
    public static final int LOAN_PERIOD_DAYS = 7;        // 7-day borrowing period

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Issues a book to a student.
     * Decreases available_quantity by 1 and sets the due date to 7 days from issue date.
     */
    @Transactional
    public BorrowRecordDto borrowBook(Long studentId, Long bookId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        if (!student.isActive()) {
            throw new IllegalStateException("Student account is inactive");
        }

        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("No copies available for borrowing for book: " + book.getTitle());
        }

        // Deduct available quantity
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_PERIOD_DAYS);

        BorrowRecord record = new BorrowRecord(student, book, issueDate, dueDate);
        BorrowRecord saved = borrowRecordRepository.save(record);

        return mapToDto(saved);
    }

    /**
     * Processes a book return.
     * Checks if already returned, records return date, calculates final penalty (₹5/day),
     * marks status as RETURNED, and increments available_quantity by 1.
     */
    @Transactional
    public BorrowRecordDto returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with ID: " + recordId));

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Book has already been returned on " + record.getReturnDate());
        }

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");

        // Calculate penalty if returned past due date
        double penalty = 0.0;
        if (returnDate.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            penalty = overdueDays * DAILY_PENALTY_RATE;
        }
        record.setPenaltyAmount(penalty);
        if (penalty == 0.0) {
            record.setPenaltyPaid(true);
        }

        // Restore book stock
        Book book = bookRepository.findByIdForUpdate(record.getBook().getId())
                .orElseThrow(() -> new IllegalStateException("Book not found for borrow record: " + recordId));
        if (book.getAvailableQuantity() >= book.getQuantity()) {
            throw new IllegalStateException("Book inventory is already at its total quantity");
        }
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        BorrowRecord updated = borrowRecordRepository.save(record);
        return mapToDto(updated);
    }

    /**
     * Marks penalty as paid.
     */
    @Transactional
    public BorrowRecordDto payPenalty(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with ID: " + recordId));

        if (!"RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("A penalty can only be paid after the book has been returned");
        }
        if (record.getPenaltyAmount() == null || record.getPenaltyAmount() <= 0.0) {
            throw new IllegalStateException("There is no penalty due for this borrowing record");
        }
        if (Boolean.TRUE.equals(record.getPenaltyPaid())) {
            throw new IllegalStateException("The penalty has already been paid");
        }

        record.setPenaltyPaid(true);
        BorrowRecord updated = borrowRecordRepository.save(record);
        return mapToDto(updated);
    }

    public List<BorrowRecordDto> getStudentBorrowings(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        return borrowRecordRepository.findByStudentOrderByIdDesc(student)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BorrowRecordDto> getAllBorrowRecords() {
        return borrowRecordRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BorrowRecordDto> getOverdueRecords() {
        LocalDate today = LocalDate.now();
        return borrowRecordRepository.findOverdueRecords(today)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps BorrowRecord entity to DTO with dynamic overdue calculations.
     */
    public BorrowRecordDto mapToDto(BorrowRecord record) {
        BorrowRecordDto dto = new BorrowRecordDto();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudent().getId());
        dto.setStudentName(record.getStudent().getName());
        dto.setStudentEmail(record.getStudent().getEmail());
        dto.setStudentPhone(record.getStudent().getPhone());

        dto.setBookId(record.getBook().getId());
        dto.setBookTitle(record.getBook().getTitle());
        dto.setBookAuthor(record.getBook().getAuthor());
        dto.setBookCategory(record.getBook().getCategory());

        dto.setIssueDate(record.getIssueDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setStatus(record.getStatus());
        dto.setPenaltyPaid(record.getPenaltyPaid());

        LocalDate today = LocalDate.now();

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            dto.setPenaltyAmount(record.getPenaltyAmount());
            if (record.getReturnDate() != null && record.getReturnDate().isAfter(record.getDueDate())) {
                long days = ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate());
                dto.setOverdueDays(days);
                dto.setEstimatedPenalty(days * DAILY_PENALTY_RATE);
                dto.setOverdue(true);
            } else {
                dto.setOverdueDays(0);
                dto.setEstimatedPenalty(0.0);
                dto.setOverdue(false);
            }
        } else {
            // Active loan (BORROWED)
            if (today.isAfter(record.getDueDate())) {
                long days = ChronoUnit.DAYS.between(record.getDueDate(), today);
                dto.setOverdueDays(days);
                dto.setEstimatedPenalty(days * DAILY_PENALTY_RATE);
                dto.setPenaltyAmount(days * DAILY_PENALTY_RATE);
                dto.setOverdue(true);
            } else {
                dto.setOverdueDays(0);
                dto.setEstimatedPenalty(0.0);
                dto.setPenaltyAmount(0.0);
                dto.setOverdue(false);
            }
        }

        return dto;
    }
}
