package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Clock;

/** A profissional registra que a cliente não compareceu. */
public class MarkAppointmentNoShowUseCase {

    private final AppointmentGateway appointmentGateway;
    private final Clock clock;

    public MarkAppointmentNoShowUseCase(AppointmentGateway appointmentGateway, Clock clock) {
        this.appointmentGateway = appointmentGateway;
        this.clock = clock;
    }

    public Appointment execute(long appointmentId) {
        Appointment appointment = appointmentGateway.getById(appointmentId);
        appointment.markNoShow(clock.instant());
        return appointmentGateway.save(appointment);
    }
}
