package com.adoptr.adoptr.shared.util;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagingTest {

    private static final Sort SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    @Test
    void paginaNegativaFalla() {
        assertThatThrownBy(() -> Paging.pageable(-1, 20, SORT))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR")
                .hasMessageContaining("page: debe ser mayor o igual a 0");
    }

    @Test
    void pageSizeCeroFalla() {
        assertThatThrownBy(() -> Paging.pageable(0, 0, SORT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pageSize: debe estar entre 1 y 50");
    }

    @Test
    void pageSize51Falla() {
        assertThatThrownBy(() -> Paging.pageable(0, 51, SORT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pageSize: debe estar entre 1 y 50");
    }

    @Test
    void pageSize50EsValidoYConservaElOrden() {
        Pageable pageable = Paging.pageable(3, 50, SORT);

        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort()).isEqualTo(SORT);
    }

    @Test
    void valoresPorDefecto() {
        assertThat(Paging.DEFAULT_PAGE_SIZE).isEqualTo(20);
        assertThat(Paging.MAX_PAGE_SIZE).isEqualTo(50);
    }
}
