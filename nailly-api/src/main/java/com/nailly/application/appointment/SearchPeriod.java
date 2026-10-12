package com.nailly.application.appointment;

import com.nailly.domain.shared.DomainException;

import java.time.Duration;
import java.time.Instant;

/** Período de consulta da agenda: [from, to), com no máximo 31 dias. */
final class SearchPeriod {

    static final Duration MAX_LENGTH = Duration.ofDays(31);

    private SearchPeriod() {
    }

    static void validate(Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw new DomainException("Informe um período válido: o fim deve ser depois do início.");
        }
        if (Duration.between(from, to).compareTo(MAX_LENGTH) > 0) {
            throw new DomainException("Consulte no máximo 31 dias por vez.");
        }
    }
}
