package com.adoptr.adoptr.shared.util;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Valida los parámetros {@code page} y {@code pageSize} de los listados. */
public final class Paging {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    private Paging() {
    }

    public static Pageable pageable(int page, int pageSize, Sort sort) {
        if (page < 0) {
            throw invalid("page: debe ser mayor o igual a 0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw invalid("pageSize: debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, pageSize, sort);
    }

    private static BadRequestException invalid(String detail) {
        return new BadRequestException(CommonError.VALIDATION_ERROR.getCode(),
                CommonError.VALIDATION_ERROR.getMessage() + " (" + detail + ")");
    }
}
