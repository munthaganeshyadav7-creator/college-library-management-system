package com.college.library.entity;

import jakarta.persistence.*;

/**
 * Book Entity representing the library inventory.
 * Maps to the 'books' table in MySQL database.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    public Book() {
    }

    public Book(String title, String author, String category, Integer quantity, Integer availableQuantity) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
