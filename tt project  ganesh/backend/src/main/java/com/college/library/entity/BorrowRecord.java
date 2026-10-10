package com.college.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a book issue/borrow transaction.
 * Maps to MySQL 'borrow_records' table.
 */
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(nullable = false, length = 20)
    private String status = "ISSUED"; // ISSUED, RETURNED, OVERDUE

    @Column(name = "penalty_amount", nullable = false)
    private Double penaltyAmount = 0.0;

    @Column(name = "penalty_paid", nullable = false)
    private boolean penaltyPaid = false;

    public BorrowRecord() {
    }

    public BorrowRecord(User student, Book book, LocalDate issueDate, LocalDate dueDate) {
        this.student = student;
        this.book = book;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = "ISSUED";
        this.penaltyAmount = 0.0;
        this.penaltyPaid = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(Double penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public boolean isPenaltyPaid() {
        return penaltyPaid;
    }

    public void setPenaltyPaid(boolean penaltyPaid) {
        this.penaltyPaid = penaltyPaid;
    }
}
