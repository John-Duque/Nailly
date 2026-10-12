package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

public class GetAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;

    public GetAppointmentUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public Appointment execute(long appointmentId) {
        return appointmentGateway.getById(appointmentId);
    }
}
