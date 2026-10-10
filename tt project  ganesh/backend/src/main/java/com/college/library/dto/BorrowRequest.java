package com.college.library.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for book borrow request.
 * If studentId is null, the currently authenticated student user is used.
 */
public class BorrowRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private Long studentId; // Optional for student self-service, required for librarian issuing

    private String studentEmail; // Optional lookup convenience for librarian issuing

    public BorrowRequest() {
    }

    public BorrowRequest(Long bookId, Long studentId) {
        this.bookId = bookId;
        this.studentId = studentId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}
