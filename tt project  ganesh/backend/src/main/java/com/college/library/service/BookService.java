package com.college.library.service;

import com.college.library.dto.BookDto;
import com.college.library.entity.Book;
import com.college.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing book inventory, searching, and catalog modifications.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Retrieve all books or filter by keyword/category.
     */
    public List<BookDto> getAllBooks(String search, String category) {
        List<Book> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookRepository.searchBooks(search.trim());
        } else if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            books = bookRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            books = bookRepository.findAll();
        }

        return books.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get single book by ID.
     */
    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
        return mapToDto(book);
    }

    /**
     * Add a new book to inventory.
     */
    @Transactional
    public BookDto addBook(BookDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(dto.getAuthor().trim());
        book.setCategory(dto.getCategory().trim());
        book.setQuantity(dto.getQuantity());
        // By default, newly added book has all copies available
        book.setAvailableQuantity(dto.getAvailableQuantity() != null ? dto.getAvailableQuantity() : dto.getQuantity());

        Book saved = bookRepository.save(book);
        return mapToDto(saved);
    }

    /**
     * Update existing book details.
     */
    @Transactional
    public BookDto updateBook(Long id, BookDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));

        int borrowedCopies = book.getQuantity() - book.getAvailableQuantity();
        if (dto.getQuantity() < borrowedCopies) {
            throw new IllegalArgumentException("Cannot reduce total quantity below currently borrowed copies (" + borrowedCopies + ")");
        }

        book.setTitle(dto.getTitle().trim());
        book.setAuthor(dto.getAuthor().trim());
        book.setCategory(dto.getCategory().trim());
        
        int difference = dto.getQuantity() - book.getQuantity();
        book.setQuantity(dto.getQuantity());
        book.setAvailableQuantity(book.getAvailableQuantity() + difference);

        Book updated = bookRepository.save(book);
        return mapToDto(updated);
    }

    /**
     * Delete book from inventory if no active borrowings.
     */
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
        
        int borrowedCopies = book.getQuantity() - book.getAvailableQuantity();
        if (borrowedCopies > 0) {
            throw new IllegalStateException("Cannot delete book. " + borrowedCopies + " copies are currently issued to students.");
        }

        bookRepository.delete(book);
    }

    public BookDto mapToDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getQuantity(),
                book.getAvailableQuantity()
        );
    }
}
