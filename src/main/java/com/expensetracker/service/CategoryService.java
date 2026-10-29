package com.expensetracker.service;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    public CategoryResponse create(Long userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        return toResponse(categoryRepository.save(new Category(name, user)));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return categoryRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toResponse).toList();
    }

    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = find(userId, categoryId);
        String name = request.name().trim();
        if (!name.equalsIgnoreCase(category.getName())
                && categoryRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        category.setName(name);   // dirty checking saves this automatically at commit
        return toResponse(category);
    }

    public void delete(Long userId, Long categoryId) {
        Category category = find(userId, categoryId);
        if (expenseRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Category is still used by expenses and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    private Category find(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category " + categoryId + " not found for user " + userId));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
