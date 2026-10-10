package com.college.library.dto;

import jakarta.validation.constraints.NotNull;

public class BorrowRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private Long studentId; // Optional: If empty, authenticated student is used

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
}
