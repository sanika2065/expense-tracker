package com.expensetracker.service;

import com.expensetracker.dto.CategorySpendingResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.MonthlyReportResponse;
import com.expensetracker.dto.PageResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        Category category = findCategory(userId, request.categoryId());
        Expense expense = new Expense(request.amount(), clean(request.description()),
                request.expenseDate(), user, category);
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long userId, Long expenseId) {
        return toResponse(findExpense(userId, expenseId));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> search(Long userId, Long categoryId,
                                                LocalDate from, LocalDate to,
                                                int page, int size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }
        requireUser(userId);

        Specification<Expense> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        List<ExpenseResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    public ExpenseResponse update(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = findExpense(userId, expenseId);
        Category category = findCategory(userId, request.categoryId());
        expense.setAmount(request.amount());
        expense.setDescription(clean(request.description()));
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(category);   // dirty checking writes the UPDATE at commit
        return toResponse(expense);
    }

    public void delete(Long userId, Long expenseId) {
        expenseRepository.delete(findExpense(userId, expenseId));
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse monthlyReport(Long userId, int year, int month) {
        requireUser(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        List<CategorySpendingResponse> rows = expenseRepository
                .monthlyReport(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        BigDecimal total = rows.stream()
                .map(CategorySpendingResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlyReportResponse(userId, year, month, total, rows);
    }

    // ----- helpers -----

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
    }

    private Category findCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category " + categoryId + " not found for user " + userId));
    }

    private Expense findExpense(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense " + expenseId + " not found for user " + userId));
    }

    private String clean(String text) {
        return text == null ? null : text.trim();
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDescription(),
                e.getExpenseDate(), e.getCategory().getId(), e.getCategory().getName(),
                e.getCreatedAt());
    }
}
