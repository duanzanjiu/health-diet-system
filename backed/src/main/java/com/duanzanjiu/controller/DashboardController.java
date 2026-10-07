package com.duanzanjiu.controller;
import org.springframework.security.core.context.SecurityContextHolder;
import com.duanzanjiu.common.Result;
import com.duanzanjiu.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> adminStats() {
        return Result.ok(dashboardService.getAdminStats());
    }

    @GetMapping("/admin/charts")
    public Result<Map<String, Object>> adminCharts() {
        return Result.ok(dashboardService.getAdminCharts());
    }

    @GetMapping("/user/overview")
    public Result<Map<String, Object>> userOverview() {
        Long userId = getCurrentUserId();
        return Result.ok(dashboardService.getUserOverview(userId));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Long) principal;
    }
}
