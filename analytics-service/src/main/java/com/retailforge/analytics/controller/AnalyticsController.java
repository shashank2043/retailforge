package com.retailforge.analytics.controller;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.analytics.dto.DashboardResponse;
import com.retailforge.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('STORE_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardMetrics() {
        DashboardResponse dashboard = analyticsService.getDashboardMetrics();
        ApiResponse<DashboardResponse> apiResponse = new ApiResponse<>(true, "Analytics dashboard metrics loaded successfully.", dashboard);
        return ResponseEntity.ok(apiResponse);
    }
}
