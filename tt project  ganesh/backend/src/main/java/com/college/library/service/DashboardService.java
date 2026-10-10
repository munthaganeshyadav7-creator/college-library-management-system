package com.college.library.service;

import com.college.library.dto.DashboardStatsDto;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service aggregating real-time analytics and statistics for Student, Librarian, and Admin dashboards.
 */
@Service
public class DashboardService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    public DashboardService(BookRepository bookRepository,
                            BorrowRecordRepository borrowRecordRepository,
                            UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get system-wide dashboard statistics (Admin / Librarian / Public summary).
     */
    public DashboardStatsDto getSystemStats(String currentUserEmail) {
        DashboardStatsDto dto = new DashboardStatsDto();

        long totalTitles = bookRepository.count();
        Long totalCopies = bookRepository.sumTotalCopies();
        Long availableCopies = bookRepository.sumAvailableCopies();
        long activeLoans = borrowRecordRepository.countByStatus("ISSUED");
        long returnedBooks = borrowRecordRepository.countByStatus("RETURNED");
        long registeredStudents = userRepository.countByRole(Role.STUDENT);
        long overdueLoans = borrowRecordRepository.countOverdueIssuedRecords(LocalDate.now());

        Double totalPenalties = borrowRecordRepository.sumTotalPenalties();
        Double unpaidPenalties = borrowRecordRepository.sumUnpaidPenalties();
        Double paidPenalties = borrowRecordRepository.sumPaidPenalties();

        dto.setTotalTitles(totalTitles);
        dto.setTotalCopies(totalCopies != null ? totalCopies : 0);
        dto.setAvailableCopies(availableCopies != null ? availableCopies : 0);
        dto.setIssuedCopies(dto.getTotalCopies() - dto.getAvailableCopies());
        dto.setActiveLoans(activeLoans);
        dto.setReturnedBooks(returnedBooks);
        dto.setRegisteredStudents(registeredStudents);
        dto.setOverdueLoans(overdueLoans);
        dto.setTotalRecordedPenalties(totalPenalties != null ? totalPenalties : 0.0);
        dto.setUnpaidPenalties(unpaidPenalties != null ? unpaidPenalties : 0.0);
        dto.setPaidPenalties(paidPenalties != null ? paidPenalties : 0.0);

        // If currentUserEmail is present and is a student, compute student metrics
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            userRepository.findByEmail(currentUserEmail).ifPresent(user -> {
                List<BorrowRecord> userRecords = borrowRecordRepository.findByStudentOrderByIssueDateDesc(user);
                long myActive = 0;
                long myOverdue = 0;
                double myUnpaid = 0.0;
                LocalDate today = LocalDate.now();

                for (BorrowRecord r : userRecords) {
                    if ("ISSUED".equalsIgnoreCase(r.getStatus())) {
                        myActive++;
                        if (today.isAfter(r.getDueDate())) {
                            myOverdue++;
                            long late = ChronoUnit.DAYS.between(r.getDueDate(), today);
                            myUnpaid += (late * 5.0);
                        }
                    } else if ("RETURNED".equalsIgnoreCase(r.getStatus()) && !r.isPenaltyPaid() && r.getPenaltyAmount() > 0) {
                        myUnpaid += r.getPenaltyAmount();
                    }
                }

                dto.setMyTotalBorrowed(userRecords.size());
                dto.setMyActiveLoans(myActive);
                dto.setMyOverdueLoans(myOverdue);
                dto.setMyUnpaidPenalties(myUnpaid);
            });
        }

        return dto;
    }
}
