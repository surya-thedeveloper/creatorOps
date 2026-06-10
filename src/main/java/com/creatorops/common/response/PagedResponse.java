package com.creatorops.common.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    PaginationMetadata pagination
) {
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
            )
        );
    }

    public record PaginationMetadata(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isLast
    ) {}
}
