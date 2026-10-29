package com.expensetracker.dto;

import java.math.BigDecimal;

/** One row of the monthly report: total spent in one category. */
public record CategorySpendingResponse(String category, BigDecimal total, Long expenseCount) { }
