package com.college.library.controller;

import com.college.library.dto.BorrowRecordDto;
import com.college.library.dto.DashboardStatsDto;
import com.college.library.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard statistics for overview counters.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Penalty summary metrics (total, paid, unpaid).
     */
    @GetMapping("/dashboard/penalties")
    public ResponseEntity<Map<String, Object>> getPenaltiesSummary() {
        Map<String, Object> summary = dashboardService.getPenaltiesSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Full borrowing activity report.
     */
    @GetMapping("/reports/borrowing")
    public ResponseEntity<List<BorrowRecordDto>> getBorrowingReport() {
        List<BorrowRecordDto> report = dashboardService.getBorrowingReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Overdue loans report.
     */
    @GetMapping("/reports/overdue")
    public ResponseEntity<List<BorrowRecordDto>> getOverdueReport() {
        List<BorrowRecordDto> report = dashboardService.getOverdueReport();
        return ResponseEntity.ok(report);
    }
}
