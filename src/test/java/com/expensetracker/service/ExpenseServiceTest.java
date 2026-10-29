package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.expensetracker.dto.CategorySpendingResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.MonthlyReportResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ExpenseService expenseService;

    @Test
    void create_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ExpenseRequest request = new ExpenseRequest(new BigDecimal("10.00"), "Tea", LocalDate.now(), 1L);

        assertThrows(ResourceNotFoundException.class, () -> expenseService.create(99L, request));
    }

    @Test
    void create_savesExpenseAndReturnsResponse() {
        User user = new User("Asha", "asha@example.com");
        Category category = new Category("Food", user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("120.50"), "  Lunch  ", LocalDate.of(2026, 10, 1), 5L);
        ExpenseResponse response = expenseService.create(1L, request);

        assertEquals(new BigDecimal("120.50"), response.amount());
        assertEquals("Lunch", response.description());     // trimmed
        assertEquals("Food", response.categoryName());
    }

    @Test
    void search_rejectsFromDateAfterToDate() {
        assertThrows(IllegalArgumentException.class, () -> expenseService.search(
                1L, null, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1), 0, 10));
    }

    @Test
    void monthlyReport_sumsCategoryTotals() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(expenseRepository.monthlyReport(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new CategorySpendingResponse("Food", new BigDecimal("30.50"), 2L),
                        new CategorySpendingResponse("Travel", new BigDecimal("20.00"), 1L)));

        MonthlyReportResponse report = expenseService.monthlyReport(1L, 2026, 10);

        assertEquals(0, new BigDecimal("50.50").compareTo(report.totalSpent()));
        assertEquals(2, report.categories().size());
    }
}
