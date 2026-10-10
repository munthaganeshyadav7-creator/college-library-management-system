package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.BookDto;
import com.college.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Book catalog operations and inventory management.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get all books or search books by keyword / category.
     * Accessible publicly and by all logged-in users.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookDto>>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        List<BookDto> books = bookService.getAllBooks(search, category);
        return ResponseEntity.ok(ApiResponse.ok("Books retrieved", books));
    }

    /**
     * Get single book by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDto>> getBookById(@PathVariable Long id) {
        try {
            BookDto book = bookService.getBookById(id);
            return ResponseEntity.ok(ApiResponse.ok("Book details retrieved", book));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add a new book (Librarian & Admin).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> addBook(@Valid @RequestBody BookDto bookDto) {
        try {
            BookDto created = bookService.addBook(bookDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Book added to library catalog successfully!", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add book: " + e.getMessage()));
        }
    }

    /**
     * Update book details (Librarian & Admin).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        try {
            BookDto updated = bookService.updateBook(id, bookDto);
            return ResponseEntity.ok(ApiResponse.ok("Book updated successfully!", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update book: " + e.getMessage()));
        }
    }

    /**
     * Delete book from catalog (Librarian & Admin).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(ApiResponse.ok("Book deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete book: " + e.getMessage()));
        }
    }
}
