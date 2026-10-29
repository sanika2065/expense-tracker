package com.expensetracker.controller;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Create a category for a user")
    public ResponseEntity<CategoryResponse> create(@PathVariable Long userId,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List a user's categories")
    public List<CategoryResponse> list(@PathVariable Long userId) {
        return categoryService.list(userId);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Rename a category")
    public CategoryResponse update(@PathVariable Long userId,
                                   @PathVariable Long categoryId,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(userId, categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a category (only if no expense uses it)")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long categoryId) {
        categoryService.delete(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
