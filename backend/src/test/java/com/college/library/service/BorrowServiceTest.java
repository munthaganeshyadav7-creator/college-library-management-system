package com.college.library.service;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.entity.Book;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowServiceTest {

    private BorrowRecordRepository borrowRecordRepository;
    private BookRepository bookRepository;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowRecordRepository = mock(BorrowRecordRepository.class);
        bookRepository = mock(BookRepository.class);
        borrowService = new BorrowService(
                borrowRecordRepository,
                bookRepository,
                mock(UserRepository.class)
        );
    }

    @Test
    void calculatesFifteenRupeesForThreeDaysLate() {
        LocalDate dueDate = LocalDate.now().minusDays(3);
        BorrowRecordDto dto = borrowService.mapToDto(returnedRecord(dueDate, dueDate.plusDays(3)));

        assertEquals(15.0, dto.getPenaltyAmount());
        assertEquals(3, dto.getOverdueDays());
        assertEquals(15.0, dto.getEstimatedPenalty());
    }

    @Test
    void chargesNothingWhenReturnedOnDueDate() {
        LocalDate dueDate = LocalDate.now();
        BorrowRecordDto dto = borrowService.mapToDto(returnedRecord(dueDate, dueDate));

        assertEquals(0.0, dto.getPenaltyAmount());
        assertEquals(0, dto.getOverdueDays());
        assertFalse(dto.isOverdue());
    }

    @Test
    void chargesNothingWhenReturnedBeforeDueDate() {
        LocalDate dueDate = LocalDate.now().plusDays(2);
        BorrowRecordDto dto = borrowService.mapToDto(returnedRecord(dueDate, LocalDate.now()));

        assertEquals(0.0, dto.getPenaltyAmount());
        assertEquals(0, dto.getOverdueDays());
        assertFalse(dto.isOverdue());
    }

    @Test
    void estimatesPenaltyForUnreturnedOverdueLoan() {
        LocalDate dueDate = LocalDate.now().minusDays(3);
        BorrowRecord record = new BorrowRecord(
                new User(), new Book(), dueDate.minusDays(7), dueDate
        );

        BorrowRecordDto dto = borrowService.mapToDto(record);

        assertEquals(3, dto.getOverdueDays());
        assertEquals(15.0, dto.getEstimatedPenalty());
        assertEquals(15.0, dto.getPenaltyAmount());
        assertTrue(dto.isOverdue());
    }

    @Test
    void rejectsDuplicateReturnWithoutUpdatingInventoryAgain() {
        LocalDate date = LocalDate.now();
        BorrowRecord record = returnedRecord(date, date);
        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> borrowService.returnBook(1L));

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void storesFinalThreeDayPenaltyAndRestoresStockOnReturn() {
        LocalDate dueDate = LocalDate.now().minusDays(3);
        Book book = new Book("Test Book", "Test Author", "Testing", 2, 1);
        book.setId(9L);
        BorrowRecord record = new BorrowRecord(
                new User(), book, dueDate.minusDays(7), dueDate
        );
        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(borrowRecordRepository.save(record)).thenReturn(record);

        BorrowRecordDto dto = borrowService.returnBook(1L);

        assertEquals("RETURNED", record.getStatus());
        assertEquals(LocalDate.now(), record.getReturnDate());
        assertEquals(15.0, record.getPenaltyAmount());
        assertEquals(15.0, dto.getPenaltyAmount());
        assertEquals(2, book.getAvailableQuantity());
        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(record);
    }

    @Test
    void onlyAllowsPaymentOfAnUnpaidFinalPenalty() {
        LocalDate date = LocalDate.now();
        BorrowRecord record = returnedRecord(date.minusDays(3), date);
        record.setPenaltyAmount(15.0);
        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(record)).thenReturn(record);

        borrowService.payPenalty(1L);

        assertTrue(record.getPenaltyPaid());
        assertThrows(IllegalStateException.class, () -> borrowService.payPenalty(1L));
    }

    @Test
    void rejectsPaymentBeforeReturn() {
        BorrowRecord record = new BorrowRecord(
                new User(), new Book(), LocalDate.now(), LocalDate.now().plusDays(7)
        );
        record.setPenaltyAmount(15.0);
        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> borrowService.payPenalty(1L));
        verify(borrowRecordRepository, never()).save(any());
    }

    private BorrowRecord returnedRecord(LocalDate dueDate, LocalDate returnDate) {
        BorrowRecord record = new BorrowRecord(
                new User(), new Book(), dueDate.minusDays(7), dueDate
        );
        record.setReturnDate(returnDate);
        record.setStatus("RETURNED");
        long overdueDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate));
        record.setPenaltyAmount(overdueDays * BorrowService.DAILY_PENALTY_RATE);
        return record;
    }
}
