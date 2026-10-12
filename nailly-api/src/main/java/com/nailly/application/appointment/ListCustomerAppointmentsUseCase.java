package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Instant;
import java.util.List;

/** Agendamentos da cliente num período (até 31 dias), em ordem de horário, com todos os status. */
public class ListCustomerAppointmentsUseCase {

    private final AppointmentGateway appointmentGateway;

    public ListCustomerAppointmentsUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(long customerId, Instant from, Instant to) {
        SearchPeriod.validate(from, to);
        return appointmentGateway.findByCustomerBetween(customerId, from, to);
    }
}
