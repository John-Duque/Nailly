package com.nailly.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validações reaproveitadas pelas classes de domínio. */
public final class DomainValidation {

    private DomainValidation() {
    }

    /** Exige um texto preenchido e devolve sem espaços nas pontas. */
    public static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
        return value.strip();
    }

    /** Texto opcional: vazio vira {@code null}. */
    public static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    /** Ajusta a escala sem arredondar: recusa valores com mais casas decimais que o banco guarda. */
    public static BigDecimal requireScale(BigDecimal value, int scale, String message) {
        try {
            return value.setScale(scale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new DomainException(message, e);
        }
    }
}
