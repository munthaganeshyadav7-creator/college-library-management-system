package com.college.library.repository;

import com.college.library.entity.BorrowRecord;
import com.college.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for BorrowRecord entity database operations.
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByStudentOrderByIssueDateDesc(User student);

    List<BorrowRecord> findByStatus(String status);

    List<BorrowRecord> findAllByOrderByIssueDateDesc();

    long countByStatus(String status);

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'ISSUED' AND b.dueDate < :today ORDER BY b.dueDate ASC")
    List<BorrowRecord> findOverdueIssuedRecords(@Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM BorrowRecord b WHERE b.status = 'ISSUED' AND b.dueDate < :today")
    long countOverdueIssuedRecords(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(b.penaltyAmount), 0.0) FROM BorrowRecord b")
    Double sumTotalPenalties();

    @Query("SELECT COALESCE(SUM(b.penaltyAmount), 0.0) FROM BorrowRecord b WHERE b.penaltyPaid = false AND b.penaltyAmount > 0")
    Double sumUnpaidPenalties();

    @Query("SELECT COALESCE(SUM(b.penaltyAmount), 0.0) FROM BorrowRecord b WHERE b.penaltyPaid = true")
    Double sumPaidPenalties();
}
