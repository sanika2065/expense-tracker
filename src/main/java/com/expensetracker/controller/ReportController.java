package com.expensetracker.controller;

import com.expensetracker.dto.MonthlyReportResponse;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/reports")
@Validated
@Tag(name = "Reports")
public class ReportController {

    private final ExpenseService expenseService;

    public ReportController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/monthly")
    @Operation(summary = "Total spending per category for one month")
    public MonthlyReportResponse monthly(
            @PathVariable Long userId,
            @RequestParam @Min(value = 2000, message = "year must be 2000 or later")
            @Max(value = 2100, message = "year must be 2100 or earlier") int year,
            @RequestParam @Min(value = 1, message = "month must be 1-12")
            @Max(value = 12, message = "month must be 1-12") int month) {
        return expenseService.monthlyReport(userId, year, month);
    }
}
