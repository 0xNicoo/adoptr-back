package com.adoptr.adoptr.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página de resultados del contrato HTTP: {@code { items, page, pageSize, totalItems, totalPages }}. */
public record PageDTO<T>(List<T> items, int page, int pageSize, long totalItems, int totalPages) {

    public static <E, T> PageDTO<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
