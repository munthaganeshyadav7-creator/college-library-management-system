package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.BorrowRecordDto;
import com.college.library.dto.BorrowRequest;
import com.college.library.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book borrowing, returns, overdue tracking, and fine management.
 */
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * Borrow a book.
     * Can be invoked directly by a Student, or by a Librarian on behalf of a student.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BorrowRecordDto>> borrowBook(
            @Valid @RequestBody BorrowRequest request,
            Authentication authentication) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            BorrowRecordDto record = borrowService.borrowBook(request, email);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Book issued successfully! Due date: " + record.getDueDate(), record));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process loan: " + e.getMessage()));
        }
    }

    /**
     * Get borrowing history for currently logged-in student.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getMyBorrowings(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        List<BorrowRecordDto> records = borrowService.getStudentBorrowings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Borrowing history retrieved", records));
    }

    /**
     * Get all borrowing records across the college library.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getAllBorrowings() {
        List<BorrowRecordDto> records = borrowService.getAllBorrowings();
        return ResponseEntity.ok(ApiResponse.ok("All borrow records retrieved", records));
    }

    /**
     * Get only currently overdue loans.
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getOverdueBorrowings() {
        List<BorrowRecordDto> records = borrowService.getOverdueBorrowings();
        return ResponseEntity.ok(ApiResponse.ok("Overdue loans retrieved", records));
    }

    /**
     * Process book return and finalize penalty.
     */
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BorrowRecordDto>> returnBook(@PathVariable Long id) {
        try {
            BorrowRecordDto result = borrowService.returnBook(id);
            String message = "Book returned successfully!";
            if (result.getPenaltyAmount() > 0) {
                message += " Overdue penalty calculated: \u20B9" + String.format("%.2f", result.getPenaltyAmount());
            } else {
                message += " Returned on time, no penalty incurred (\u20B90.00).";
            }
            return ResponseEntity.ok(ApiResponse.ok(message, result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to return book: " + e.getMessage()));
        }
    }

    /**
     * Mark penalty as paid.
     */
    @PutMapping("/{id}/pay-penalty")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BorrowRecordDto>> payPenalty(@PathVariable Long id) {
        try {
            BorrowRecordDto result = borrowService.payPenalty(id);
            return ResponseEntity.ok(ApiResponse.ok("Penalty marked as PAID successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update penalty: " + e.getMessage()));
        }
    }
}
