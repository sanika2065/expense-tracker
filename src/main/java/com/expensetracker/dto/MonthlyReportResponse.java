package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportResponse(
        Long userId,
        int year,
        int month,
        BigDecimal totalSpent,
        List<CategorySpendingResponse> categories) { }
