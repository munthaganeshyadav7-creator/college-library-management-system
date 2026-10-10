package com.college.library.controller;

import com.college.library.dto.ApiResponse;
import com.college.library.dto.DashboardStatsDto;
import com.college.library.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving dashboard statistics.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard stats for logged in users (Student, Librarian, Admin).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats(Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
        DashboardStatsDto stats = dashboardService.getSystemStats(email);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved", stats));
    }

    /**
     * Public summary statistics for homepage hero counter.
     */
    @GetMapping("/public-summary")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getPublicSummary() {
        DashboardStatsDto stats = dashboardService.getSystemStats(null);
        return ResponseEntity.ok(ApiResponse.ok("Public summary retrieved", stats));
    }
}
