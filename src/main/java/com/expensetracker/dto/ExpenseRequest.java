package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        @Digits(integer = 10, fraction = 2, message = "amount can have at most 10 digits and 2 decimals")
        BigDecimal amount,

        @Size(max = 255, message = "description must be at most 255 characters")
        String description,

        @NotNull(message = "expenseDate is required (format yyyy-MM-dd)")
        @PastOrPresent(message = "expenseDate cannot be in the future")
        LocalDate expenseDate,

        @NotNull(message = "categoryId is required")
        Long categoryId) { }
