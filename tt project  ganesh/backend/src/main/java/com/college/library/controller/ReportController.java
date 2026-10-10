package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.BorrowRecordDto;
import com.college.library.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for generating administrative audit and penalty reports.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Borrowing audit report.
     */
    @GetMapping("/borrowing")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getBorrowingReport(
            @RequestParam(required = false) String status) {
        List<BorrowRecordDto> report = reportService.getBorrowingReport(status);
        return ResponseEntity.ok(ApiResponse.ok("Borrowing report generated", report));
    }

    /**
     * Overdue book audit report.
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getOverdueReport() {
        List<BorrowRecordDto> report = reportService.getOverdueReport();
        return ResponseEntity.ok(ApiResponse.ok("Overdue report generated", report));
    }

    /**
     * Penalty collection audit report.
     */
    @GetMapping("/penalties")
    public ResponseEntity<ApiResponse<List<BorrowRecordDto>>> getPenaltyReport(
            @RequestParam(required = false) Boolean unpaidOnly) {
        List<BorrowRecordDto> report = reportService.getPenaltyReport(unpaidOnly);
        return ResponseEntity.ok(ApiResponse.ok("Penalty report generated", report));
    }
}
