package com.recruitment.controller;

import com.recruitment.dto.Result;
import com.recruitment.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public Result<Map<String, Object>> getDashboardStatistics() {
        return statisticsService.getDashboardStatistics();
    }
}
