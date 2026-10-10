package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.BookDto;
import com.college.library.entity.Book;
import com.college.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Lists all books or filters by search keyword (title, author, category).
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam(value = "search", required = false) String search) {
        List<Book> books = bookService.searchBooks(search);
        return ResponseEntity.ok(books);
    }

    /**
     * Gets a single book by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return ResponseEntity.ok(book);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Adds a new book to the library inventory.
     */
    @PostMapping
    public ResponseEntity<?> addBook(@Valid @RequestBody BookDto bookDto) {
        try {
            Book created = bookService.addBook(bookDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Book added successfully to catalog", created));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Updates an existing book.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
        try {
            Book updated = bookService.updateBook(id, bookDto);
            return ResponseEntity.ok(ApiResponse.ok("Book updated successfully", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Deletes a book if not currently borrowed.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(ApiResponse.ok("Book deleted successfully"));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}
