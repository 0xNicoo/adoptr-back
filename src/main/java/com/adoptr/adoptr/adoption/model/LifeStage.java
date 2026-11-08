package com.adoptr.adoptr.adoption.model;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Etapa de vida calculada a partir de la fecha de nacimiento estimada (no se persiste).
 * Cachorro: menos de 12 meses; adulto: de 12 meses a menos de 8 años; mayor: 8 años o más.
 */
public enum LifeStage {
    PUPPY, ADULT, SENIOR;

    static final int ADULT_FROM_MONTHS = 12;
    static final int SENIOR_FROM_MONTHS = 96;

    public static LifeStage of(YearMonth birth, YearMonth current) {
        long months = birth.until(current, ChronoUnit.MONTHS);
        if (months < ADULT_FROM_MONTHS) {
            return PUPPY;
        }
        return months < SENIOR_FROM_MONTHS ? ADULT : SENIOR;
    }
}
