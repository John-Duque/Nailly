package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;

/** A profissional marca o atendimento como realizado. */
public class CompleteAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public CompleteAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    public Appointment execute(long appointmentId) {
        Appointment appointment = appointmentGateway.getById(appointmentId);
        appointment.complete(clock.instant());
        return appointmentGateway.save(appointment);
    }
}
