package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;

/**
 * Cancela um agendamento. Não existe exclusão: o registro fica com status CANCELED
 * para manter o histórico da cliente e da profissional.
 */
public class CancelAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public CancelAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    public Appointment execute(long appointmentId, String reason) {
        Appointment appointment = appointmentGateway.getById(appointmentId);
        appointment.cancel(reason, clock.instant());
        return appointmentGateway.save(appointment);
    }
}
