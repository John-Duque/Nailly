package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;

import java.time.Instant;
import java.util.List;

/** Agenda da profissional num período (até 31 dias), em ordem de horário, com todos os status. */
public class ListProfessionalAgendaUseCase {

    private final AppointmentGateway appointmentGateway;

    public ListProfessionalAgendaUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(long professionalId, Instant from, Instant to) {
        SearchPeriod.validate(from, to);
        return appointmentGateway.findByProfessionalBetween(professionalId, from, to);
    }
}
