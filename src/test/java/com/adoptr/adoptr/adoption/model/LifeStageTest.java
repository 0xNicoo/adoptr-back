package com.adoptr.adoptr.adoption.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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

    @Test
    void rangoDeCachorro() {
        assertThat(LifeStage.PUPPY.birthDateRange(CURRENT))
                .isEqualTo(new LifeStage.BirthDateRange(LocalDate.of(2025, 12, 1), null));
    }

    @Test
    void rangoDeAdulto() {
        assertThat(LifeStage.ADULT.birthDateRange(CURRENT))
                .isEqualTo(new LifeStage.BirthDateRange(LocalDate.of(2018, 12, 1), LocalDate.of(2025, 11, 1)));
    }

    @Test
    void rangoDeMayor() {
        assertThat(LifeStage.SENIOR.birthDateRange(CURRENT))
                .isEqualTo(new LifeStage.BirthDateRange(null, LocalDate.of(2018, 11, 1)));
    }

    /** Cada mes de los últimos 31 años cae exactamente en el rango de la etapa que calcula {@code of}. */
    @Test
    void rangoYEtapaCoincidenMesAMes() {
        for (int monthsAgo = 0; monthsAgo <= 372; monthsAgo++) {
            YearMonth birth = CURRENT.minusMonths(monthsAgo);
            LocalDate stored = birth.atDay(1);
            for (LifeStage stage : LifeStage.values()) {
                assertThat(stage.birthDateRange(CURRENT).contains(stored))
                        .as("%s nacido %s", stage, birth)
                        .isEqualTo(LifeStage.of(birth, CURRENT) == stage);
            }
        }
    }
}
