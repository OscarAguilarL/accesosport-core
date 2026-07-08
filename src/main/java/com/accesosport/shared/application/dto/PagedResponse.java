package com.accesosport.shared.application.dto;

import com.accesosport.shared.domain.query.PageResult;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PagedResponse<T> from(PageResult<T> result) {
        return new PagedResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.page() < result.totalPages() - 1,
                result.page() > 0
        );
    }
}
