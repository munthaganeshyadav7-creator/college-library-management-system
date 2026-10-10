package com.college.library.repository;

import com.college.library.entity.BorrowRecord;
import com.college.library.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BorrowRecord b WHERE b.id = :id")
    java.util.Optional<BorrowRecord> findByIdForUpdate(@Param("id") Long id);

    List<BorrowRecord> findByStudentOrderByIdDesc(User student);

    List<BorrowRecord> findByStudentAndStatus(User student, String status);

    List<BorrowRecord> findByStatusOrderByIdDesc(String status);

    List<BorrowRecord> findAllByOrderByIdDesc();

    long countByStatus(String status);

    // Overdue loans: unreturned (status = BORROWED) and dueDate is strictly before today
    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'BORROWED' AND b.dueDate < :today ORDER BY b.dueDate ASC")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM BorrowRecord b WHERE b.status = 'BORROWED' AND b.dueDate < :today")
    long countOverdueRecords(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(b.penaltyAmount), 0.0) FROM BorrowRecord b")
    Double sumTotalPenalties();

    @Query("SELECT COALESCE(SUM(b.penaltyAmount), 0.0) FROM BorrowRecord b WHERE b.penaltyPaid = false AND b.penaltyAmount > 0")
    Double sumUnpaidPenalties();
}
