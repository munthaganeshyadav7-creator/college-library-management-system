package com.college.library.service;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.dto.DashboardStatsDto;
import com.college.library.entity.Role;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRecordRepository;
import com.college.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BorrowService borrowService;

    public DashboardService(BookRepository bookRepository,
                            BorrowRecordRepository borrowRecordRepository,
                            UserRepository userRepository,
                            BorrowService borrowService) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
        this.borrowService = borrowService;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalBookTitles(bookRepository.count());
        stats.setTotalCopies(bookRepository.sumTotalCopies());
        stats.setAvailableCopies(bookRepository.sumAvailableCopies());

        stats.setRegisteredStudents(userRepository.countByRole(Role.STUDENT));

        stats.setActiveLoans(borrowRecordRepository.countByStatus("BORROWED"));
        stats.setReturnedLoans(borrowRecordRepository.countByStatus("RETURNED"));

        LocalDate today = LocalDate.now();
        stats.setOverdueLoans(borrowRecordRepository.countOverdueRecords(today));

        // Penalty calculations
        // Dynamic sum includes returned fines + active overdue estimated fines
        List<BorrowRecordDto> all = borrowService.getAllBorrowRecords();
        double totalPenalties = 0.0;
        double unpaidPenalties = 0.0;

        for (BorrowRecordDto rec : all) {
            double fine = rec.getPenaltyAmount() != null ? rec.getPenaltyAmount() : rec.getEstimatedPenalty();
            totalPenalties += fine;
            if (fine > 0 && !Boolean.TRUE.equals(rec.getPenaltyPaid())) {
                unpaidPenalties += fine;
            }
        }

        stats.setTotalPenalties(totalPenalties);
        stats.setUnpaidPenalties(unpaidPenalties);

        return stats;
    }

    public Map<String, Object> getPenaltiesSummary() {
        DashboardStatsDto stats = getDashboardStats();
        Map<String, Object> map = new HashMap<>();
        map.put("totalPenalties", stats.getTotalPenalties());
        map.put("unpaidPenalties", stats.getUnpaidPenalties());
        map.put("paidPenalties", stats.getTotalPenalties() - stats.getUnpaidPenalties());
        map.put("overdueLoansCount", stats.getOverdueLoans());
        return map;
    }

    public List<BorrowRecordDto> getBorrowingReport() {
        return borrowService.getAllBorrowRecords();
    }

    public List<BorrowRecordDto> getOverdueReport() {
        return borrowService.getOverdueRecords();
    }
}
