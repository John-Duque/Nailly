package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.nailly.application.appointment.AppointmentFixtures.NOW;
import static com.nailly.application.appointment.AppointmentFixtures.START;
import static com.nailly.application.appointment.AppointmentFixtures.clockAt;
import static com.nailly.application.appointment.AppointmentFixtures.confirmed;
import static com.nailly.application.appointment.AppointmentFixtures.pending;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Casos de uso que só mudam o status de um agendamento já gravado. */
@ExtendWith(MockitoExtension.class)
class AppointmentStatusUseCasesTest {

    private static final long APPOINTMENT_ID = 50L;
    private static final Clock BEFORE_DEADLINE = clockAt(NOW.plusSeconds(60));
    private static final Clock AFTER_START = clockAt(START.plusSeconds(3600));

    @Mock
    private AppointmentGateway appointmentGateway;

    /** Simula o agendamento gravado e usa o getById de verdade da interface. */
    private void given(Appointment appointment) {
        when(appointmentGateway.getById(APPOINTMENT_ID)).thenCallRealMethod();
        when(appointmentGateway.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(appointmentGateway.save(appointment)).thenReturn(appointment);
    }

    @Nested
    @DisplayName("Confirmar pedido")
    class Confirm {

        @Test
        @DisplayName("Confirma pedido pendente e salva")
        void confirmsPendingRequest() {
            Appointment appointment = pending();
            given(appointment);

            Appointment result = new ConfirmAppointmentUseCase(appointmentGateway, BEFORE_DEADLINE)
                    .execute(APPOINTMENT_ID);

            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.getDecidedAt()).isEqualTo(BEFORE_DEADLINE.instant());
            verify(appointmentGateway).save(appointment);
        }

        @Test
        @DisplayName("Não salva quando o domínio recusa")
        void doesNotSaveWhenDomainRefuses() {
            when(appointmentGateway.getById(APPOINTMENT_ID)).thenCallRealMethod();
            when(appointmentGateway.findById(APPOINTMENT_ID)).thenReturn(Optional.of(confirmed()));
            ConfirmAppointmentUseCase useCase = new ConfirmAppointmentUseCase(appointmentGateway, BEFORE_DEADLINE);

            assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Só é possível confirmar um pedido pendente.");
            verify(appointmentGateway, never()).save(any());
        }

        @Test
        @DisplayName("Agendamento inexistente dá não encontrado")
        void unknownAppointmentIsNotFound() {
            when(appointmentGateway.getById(APPOINTMENT_ID)).thenCallRealMethod();
            when(appointmentGateway.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());
            ConfirmAppointmentUseCase useCase = new ConfirmAppointmentUseCase(appointmentGateway, BEFORE_DEADLINE);

            assertThatThrownBy(() -> useCase.execute(APPOINTMENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Agendamento não encontrado.");
        }
    }

    @Test
    @DisplayName("Busca o agendamento")
    void getsAppointment() {
        Appointment appointment = confirmed();
        when(appointmentGateway.getById(APPOINTMENT_ID)).thenCallRealMethod();
        when(appointmentGateway.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThat(new GetAppointmentUseCase(appointmentGateway).execute(APPOINTMENT_ID)).isSameAs(appointment);
    }

    @Test
    @DisplayName("Recusa pedido pendente com motivo")
    void rejectsPendingRequest() {
        Appointment appointment = pending();
        given(appointment);

        Appointment result = new RejectAppointmentUseCase(appointmentGateway, BEFORE_DEADLINE)
                .execute(APPOINTMENT_ID, "Agenda cheia");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.REJECTED);
        assertThat(result.getStatusReason()).isEqualTo("Agenda cheia");
    }

    @Test
    @DisplayName("Cancela mantendo o histórico")
    void cancelsKeepingHistory() {
        Appointment appointment = confirmed();
        given(appointment);

        Appointment result = new CancelAppointmentUseCase(appointmentGateway, BEFORE_DEADLINE)
                .execute(APPOINTMENT_ID, "Imprevisto");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(result.getCanceledAt()).isEqualTo(BEFORE_DEADLINE.instant());
        verify(appointmentGateway).save(appointment);
    }

    @Test
    @DisplayName("Conclui depois do horário marcado")
    void completesAfterStart() {
        Appointment appointment = confirmed();
        given(appointment);

        Appointment result = new CompleteAppointmentUseCase(appointmentGateway, AFTER_START).execute(APPOINTMENT_ID);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Marca falta depois do horário marcado")
    void marksNoShowAfterStart() {
        Appointment appointment = confirmed();
        given(appointment);

        Appointment result = new MarkAppointmentNoShowUseCase(appointmentGateway, AFTER_START)
                .execute(APPOINTMENT_ID);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    @Nested
    @DisplayName("Expirar pedidos pendentes")
    class ExpirePending {

        @Test
        @DisplayName("Expira todos os pedidos pendentes fora do prazo")
        void expiresEveryPendingRequestPastDeadline() {
            Instant afterDeadline = NOW.plusSeconds(3600);
            Appointment first = pending();
            Appointment second = pending();
            when(appointmentGateway.findPendingExpiredUntil(afterDeadline)).thenReturn(List.of(first, second));

            int expired = new ExpirePendingAppointmentsUseCase(appointmentGateway, clockAt(afterDeadline)).execute();

            assertThat(expired).isEqualTo(2);
            assertThat(first.getStatus()).isEqualTo(AppointmentStatus.EXPIRED);
            assertThat(second.getStatus()).isEqualTo(AppointmentStatus.EXPIRED);
            verify(appointmentGateway, times(2)).save(any());
        }

        @Test
        @DisplayName("Não faz nada quando nenhum pedido venceu")
        void doesNothingWhenNoRequestExpired() {
            when(appointmentGateway.findPendingExpiredUntil(NOW)).thenReturn(List.of());

            int expired = new ExpirePendingAppointmentsUseCase(appointmentGateway, clockAt(NOW)).execute();

            assertThat(expired).isZero();
            verify(appointmentGateway, never()).getById(anyLong());
            verify(appointmentGateway, never()).save(any());
        }
    }
}
