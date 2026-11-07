package com.adoptr.adoptr.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTextTest {

    @Test
    void quitaTildesYPasaAMinusculas() {
        assertThat(SearchText.normalize("Río Cuarto")).isEqualTo("rio cuarto");
        assertThat(SearchText.normalize("Ñorquincó")).isEqualTo("norquinco");
        assertThat(SearchText.normalize("GÜEMES")).isEqualTo("guemes");
    }

    @Test
    void recortaYUnificaEspacios() {
        assertThat(SearchText.normalize("  villa   carlos  paz ")).isEqualTo("villa carlos paz");
    }

    @Test
    void nullDevuelveTextoVacio() {
        assertThat(SearchText.normalize(null)).isEmpty();
    }
}
