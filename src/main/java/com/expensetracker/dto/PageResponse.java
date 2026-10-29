package com.expensetracker.dto;

import java.util.List;

/** Our own small wrapper so the JSON shape of paged results stays stable. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) { }
