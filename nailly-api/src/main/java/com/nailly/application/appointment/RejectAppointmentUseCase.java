package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;

/** A profissional recusa um pedido pendente. */
public class RejectAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public RejectAppointmentUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    public Appointment execute(long appointmentId, String reason) {
        Appointment appointment = appointmentGateway.getById(appointmentId);
        appointment.reject(reason, clock.instant());
        return appointmentGateway.save(appointment);
    }
}
