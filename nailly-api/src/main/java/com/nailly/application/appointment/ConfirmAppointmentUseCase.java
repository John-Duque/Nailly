package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;

/** A profissional aprova um pedido pendente. */
public class ConfirmAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public ConfirmAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    public Appointment execute(long appointmentId) {
        Appointment appointment = appointmentGateway.getById(appointmentId);
        appointment.confirm(clock.instant());
        return appointmentGateway.save(appointment);
    }
}
