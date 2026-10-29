package com.expensetracker.repository;

import com.expensetracker.dto.CategorySpendingResponse;
import com.expensetracker.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    /** Filtered + paged search. The EntityGraph loads each category in the same query. */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Expense> findAll(Specification<Expense> spec, Pageable pageable);

    @Query("""
            SELECT new com.expensetracker.dto.CategorySpendingResponse(
                       c.name, SUM(e.amount), COUNT(e))
            FROM Expense e JOIN e.category c
            WHERE e.user.id = :userId
              AND e.expenseDate BETWEEN :start AND :end
            GROUP BY c.id, c.name
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategorySpendingResponse> monthlyReport(@Param("userId") Long userId,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);
}
