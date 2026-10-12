package com.nailly.domain.appointment;

public enum AppointmentStatus {
    /** Aguarda a resposta da profissional. */
    PENDING,
    CONFIRMED,
    /** Recusado pela profissional. */
    REJECTED,
    /** A profissional não respondeu a tempo. */
    EXPIRED,
    CANCELED,
    COMPLETED,
    NO_SHOW;

    /** Só pedidos pendentes e confirmados ocupam a agenda. */
    public boolean occupiesSchedule() {
        return this == PENDING || this == CONFIRMED;
    }
}
