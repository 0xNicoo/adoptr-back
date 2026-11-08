package com.adoptr.adoptr.adoption.model;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class LifeStageTest {

    private static final YearMonth CURRENT = YearMonth.of(2026, 11);

    @Test
    void nacidoEsteMesEsCachorro() {
        assertThat(LifeStage.of(YearMonth.of(2026, 11), CURRENT)).isEqualTo(LifeStage.PUPPY);
    }

    @Test
    void onceMesesEsCachorro() {
        assertThat(LifeStage.of(YearMonth.of(2025, 12), CURRENT)).isEqualTo(LifeStage.PUPPY);
    }

    @Test
    void doceMesesEsAdulto() {
        assertThat(LifeStage.of(YearMonth.of(2025, 11), CURRENT)).isEqualTo(LifeStage.ADULT);
    }

    @Test
    void sieteAniosYOnceMesesEsAdulto() {
        assertThat(LifeStage.of(YearMonth.of(2018, 12), CURRENT)).isEqualTo(LifeStage.ADULT);
    }

    @Test
    void ochoAniosEsMayor() {
        assertThat(LifeStage.of(YearMonth.of(2018, 11), CURRENT)).isEqualTo(LifeStage.SENIOR);
    }
}
