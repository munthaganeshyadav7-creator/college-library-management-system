package com.college.library.service;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.repository.BorrowRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing structured audit reports for Administrator and Librarian.
 */
@Service
public class ReportService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowService borrowService;

    public ReportService(BorrowRecordRepository borrowRecordRepository,
                         BorrowService borrowService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.borrowService = borrowService;
    }

    /**
     * Complete borrowing audit report.
     */
    public List<BorrowRecordDto> getBorrowingReport(String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return borrowRecordRepository.findByStatus(status.toUpperCase().trim())
                    .stream()
                    .map(borrowService::mapToDto)
                    .collect(Collectors.toList());
        }
        return borrowRecordRepository.findAllByOrderByIssueDateDesc()
                .stream()
                .map(borrowService::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Overdue borrowing report.
     */
    public List<BorrowRecordDto> getOverdueReport() {
        return borrowRecordRepository.findOverdueIssuedRecords(LocalDate.now())
                .stream()
                .map(borrowService::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Penalty report (records with non-zero penalties, both paid and unpaid).
     */
    public List<BorrowRecordDto> getPenaltyReport(Boolean unpaidOnly) {
        List<BorrowRecordDto> all = borrowRecordRepository.findAllByOrderByIssueDateDesc()
                .stream()
                .map(borrowService::mapToDto)
                .filter(r -> r.getEstimatedPenalty() > 0 || r.getPenaltyAmount() > 0)
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(unpaidOnly)) {
            return all.stream().filter(r -> !r.isPenaltyPaid()).collect(Collectors.toList());
        }
        return all;
    }
}
