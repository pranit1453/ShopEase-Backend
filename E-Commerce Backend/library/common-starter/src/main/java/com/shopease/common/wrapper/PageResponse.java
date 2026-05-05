package com.shopease.common.wrapper;

import lombok.Builder;

import java.util.List;
/**
 * Generic pagination response wrapper used to standardize paginated API responses.
 * Contains page content along with metadata like page number, size,
 * total elements, total pages, and last page indicator.
 */
@Builder
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
