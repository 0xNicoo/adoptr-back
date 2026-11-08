package com.adoptr.adoptr.shared.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageDTOTest {

    @Test
    void fromMapeaLosItemsYCopiaLosTotales() {
        PageImpl<Integer> page = new PageImpl<>(List.of(1, 2), PageRequest.of(1, 2), 5);

        PageDTO<String> dto = PageDTO.from(page, n -> "n" + n);

        assertThat(dto.items()).containsExactly("n1", "n2");
        assertThat(dto.page()).isEqualTo(1);
        assertThat(dto.pageSize()).isEqualTo(2);
        assertThat(dto.totalItems()).isEqualTo(5);
        assertThat(dto.totalPages()).isEqualTo(3);
    }
}
