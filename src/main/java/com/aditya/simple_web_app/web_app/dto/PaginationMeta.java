package com.aditya.simple_web_app.web_app.dto;

public record PaginationMeta(
        int offset,
        int size,
        int totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
