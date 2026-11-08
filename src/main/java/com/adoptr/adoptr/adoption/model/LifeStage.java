package com.adoptr.adoptr.adoption.model;

import java.time.LocalDate;
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

    /** Fechas de nacimiento guardadas (día 1 del mes) que corresponden a esta etapa, para filtrar. */
    public BirthDateRange birthDateRange(YearMonth current) {
        return switch (this) {
            case PUPPY -> new BirthDateRange(current.minusMonths(ADULT_FROM_MONTHS - 1).atDay(1), null);
            case ADULT -> new BirthDateRange(current.minusMonths(SENIOR_FROM_MONTHS - 1).atDay(1),
                    current.minusMonths(ADULT_FROM_MONTHS).atDay(1));
            case SENIOR -> new BirthDateRange(null, current.minusMonths(SENIOR_FROM_MONTHS).atDay(1));
        };
    }

    /** Rango inclusivo; un borde {@code null} significa sin límite. */
    public record BirthDateRange(LocalDate from, LocalDate to) {
        public boolean contains(LocalDate date) {
            return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
        }
    }
}
