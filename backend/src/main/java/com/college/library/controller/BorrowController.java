package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.BorrowRecordDto;
import com.college.library.dto.BorrowRequest;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.service.BorrowService;
import com.college.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;
    private final UserService userService;

    public BorrowController(BorrowService borrowService, UserService userService) {
        this.borrowService = borrowService;
        this.userService = userService;
    }

    /**
     * Issues/borrows a book.
     * Can be invoked by a student for themselves, or by a librarian for any student.
     */
    @PostMapping
    public ResponseEntity<?> borrowBook(@Valid @RequestBody BorrowRequest request, Authentication authentication) {
        try {
            Long studentId = request.getStudentId();

            if (authentication != null) {
                User currentUser = userService.findByEmail(authentication.getName());
                if (currentUser.getRole() == Role.STUDENT) {
                    studentId = currentUser.getId(); // Student borrows for self
                }
            }

            if (studentId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Student ID is required to issue a book"));
            }

            BorrowRecordDto record = borrowService.borrowBook(studentId, request.getBookId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Book borrowed successfully. Due date: " + record.getDueDate(), record));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Retrieves borrowing history for the currently logged-in student.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyBorrowings(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        User student = userService.findByEmail(authentication.getName());
        List<BorrowRecordDto> records = borrowService.getStudentBorrowings(student.getId());
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves all borrowing records (Librarian/Admin).
     */
    @GetMapping
    public ResponseEntity<List<BorrowRecordDto>> getAllBorrowRecords() {
        List<BorrowRecordDto> records = borrowService.getAllBorrowRecords();
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves overdue records with calculated penalties.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordDto>> getOverdueRecords() {
        List<BorrowRecordDto> overdueRecords = borrowService.getOverdueRecords();
        return ResponseEntity.ok(overdueRecords);
    }

    /**
     * Processes return of a book and computes the final penalty.
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            BorrowRecordDto returned = borrowService.returnBook(id);
            String message = "Book returned successfully.";
            if (returned.getPenaltyAmount() > 0) {
                message += " Late penalty recorded: ₹" + returned.getPenaltyAmount();
            } else {
                message += " Returned on time (₹0 penalty).";
            }
            return ResponseEntity.ok(ApiResponse.ok(message, returned));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Marks penalty as paid.
     */
    @PutMapping("/{id}/pay-penalty")
    public ResponseEntity<?> payPenalty(@PathVariable Long id) {
        try {
            BorrowRecordDto updated = borrowService.payPenalty(id);
            return ResponseEntity.ok(ApiResponse.ok("Penalty marked as paid", updated));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}
