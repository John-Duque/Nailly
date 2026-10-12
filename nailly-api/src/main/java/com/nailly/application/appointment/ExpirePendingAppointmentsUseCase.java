package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Rotina periódica: encerra os pedidos que a profissional não respondeu a tempo. */
public class ExpirePendingAppointmentsUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public ExpirePendingAppointmentsUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    /** Devolve quantos pedidos foram expirados. */
    public int execute() {
        Instant now = clock.instant();
        List<Appointment> expired = appointmentGateway.findPendingExpiredUntil(now);
        for (Appointment appointment : expired) {
            appointment.expire(now);
            appointmentGateway.save(appointment);
        }
        return expired.size();
    }
}
