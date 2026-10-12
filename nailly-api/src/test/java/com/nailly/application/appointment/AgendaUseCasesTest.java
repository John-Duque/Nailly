package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.nailly.application.appointment.AppointmentFixtures.CUSTOMER_ID;
import static com.nailly.application.appointment.AppointmentFixtures.PROFESSIONAL_ID;
import static com.nailly.application.appointment.AppointmentFixtures.confirmed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaUseCasesTest {

    private static final Instant FROM = Instant.parse("2026-10-12T03:00:00Z");
    private static final Instant TO = FROM.plusSeconds(7 * 86_400L);

    @Mock
    private AppointmentGateway appointmentGateway;

    @Test
    @DisplayName("Lista a agenda da profissional no período")
    void listsAgendaOfProfessional() {
        List<Appointment> agenda = List.of(confirmed());
        when(appointmentGateway.findByProfessionalBetween(PROFESSIONAL_ID, FROM, TO)).thenReturn(agenda);

        assertThat(new ListProfessionalAgendaUseCase(appointmentGateway).execute(PROFESSIONAL_ID, FROM, TO))
                .isEqualTo(agenda);
    }

    @Test
    @DisplayName("Lista os agendamentos da cliente no período")
    void listsAppointmentsOfCustomer() {
        List<Appointment> appointments = List.of(confirmed());
        when(appointmentGateway.findByCustomerBetween(CUSTOMER_ID, FROM, TO)).thenReturn(appointments);

        assertThat(new ListCustomerAppointmentsUseCase(appointmentGateway).execute(CUSTOMER_ID, FROM, TO))
                .isEqualTo(appointments);
    }

    @Test
    @DisplayName("Recusa período com fim antes ou igual ao início")
    void periodMustEndAfterItStarts() {
        ListProfessionalAgendaUseCase useCase = new ListProfessionalAgendaUseCase(appointmentGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, TO, FROM))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe um período válido: o fim deve ser depois do início.");
        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, FROM, FROM))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, null, TO))
                .isInstanceOf(DomainException.class);
        verifyNoInteractions(appointmentGateway);
    }

    @Test
    @DisplayName("Recusa período maior que 31 dias")
    void periodIsLimitedTo31Days() {
        ListCustomerAppointmentsUseCase useCase = new ListCustomerAppointmentsUseCase(appointmentGateway);
        Instant end = FROM.plus(SearchPeriod.MAX_LENGTH).plusSeconds(1);

        assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID, FROM, end))
                .isInstanceOf(DomainException.class)
                .hasMessage("Consulte no máximo 31 dias por vez.");
        verifyNoInteractions(appointmentGateway);
    }
}
