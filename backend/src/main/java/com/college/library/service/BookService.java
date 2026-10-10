package com.college.library.service;

import com.college.library.dto.BookDto;
import com.college.library.entity.Book;
import com.college.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        return bookRepository.searchBooks(keyword.trim());
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
    }

    @Transactional
    public Book addBook(BookDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(dto.getAuthor().trim());
        book.setCategory(dto.getCategory().trim());
        book.setQuantity(dto.getQuantity());
        // For new book, available quantity defaults to total quantity if not specified
        int available = dto.getAvailableQuantity() != null ? dto.getAvailableQuantity() : dto.getQuantity();
        book.setAvailableQuantity(available);

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, BookDto dto) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));

        int borrowedCount = book.getQuantity() - book.getAvailableQuantity();
        if (dto.getQuantity() < borrowedCount) {
            throw new IllegalArgumentException(
                    "Total quantity cannot be less than currently borrowed copies (" + borrowedCount + ")");
        }

        book.setTitle(dto.getTitle().trim());
        book.setAuthor(dto.getAuthor().trim());
        book.setCategory(dto.getCategory().trim());
        
        // Adjust available quantity according to new total quantity
        int diff = dto.getQuantity() - book.getQuantity();
        book.setQuantity(dto.getQuantity());
        book.setAvailableQuantity(Math.max(0, book.getAvailableQuantity() + diff));

        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
        int borrowedCount = book.getQuantity() - book.getAvailableQuantity();
        if (borrowedCount > 0) {
            throw new IllegalStateException("Cannot delete book while copies are currently borrowed by students");
        }
        bookRepository.delete(book);
    }
}
