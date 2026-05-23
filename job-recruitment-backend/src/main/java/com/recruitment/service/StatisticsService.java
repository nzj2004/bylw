package com.recruitment.service;

import com.recruitment.dto.Result;

import java.util.Map;

public interface StatisticsService {
    Result<Map<String, Object>> getDashboardStatistics();
}
