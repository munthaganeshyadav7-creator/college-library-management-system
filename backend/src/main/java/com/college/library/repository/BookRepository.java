package com.college.library.repository;

import com.college.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    boolean existsByTitleIgnoreCase(String title);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchBooks(@Param("keyword") String keyword);

    List<Book> findByCategoryIgnoreCase(String category);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Book b")
    Long sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b")
    Long sumAvailableCopies();
}
