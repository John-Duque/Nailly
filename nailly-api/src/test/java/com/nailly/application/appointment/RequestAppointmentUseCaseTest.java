package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.nailly.application.appointment.AppointmentFixtures.CUSTOMER_ID;
import static com.nailly.application.appointment.AppointmentFixtures.NOW;
import static com.nailly.application.appointment.AppointmentFixtures.PROFESSIONAL_ID;
import static com.nailly.application.appointment.AppointmentFixtures.START;
import static com.nailly.application.appointment.AppointmentFixtures.clockAt;
import static com.nailly.application.appointment.AppointmentFixtures.professional;
import static com.nailly.application.appointment.AppointmentFixtures.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;
    @Mock
    private ProfessionalGateway professionalGateway;
    @Mock
    private SalonServiceGateway salonServiceGateway;

    private RequestAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestAppointmentUseCase(appointmentGateway, professionalGateway, salonServiceGateway,
                clockAt(NOW));
    }

    private static RequestAppointmentCommand.Builder baseCommand(List<Long> serviceIds) {
        return RequestAppointmentCommand.builder()
                .customerId(CUSTOMER_ID)
                .professionalId(PROFESSIONAL_ID)
                .serviceIds(serviceIds)
                .startAt(START);
    }

    private static RequestAppointmentCommand atLocation(List<Long> serviceIds) {
        return baseCommand(serviceIds).atLocation(3L, "Rua A, 10").build();
    }

    private static RequestAppointmentCommand homeVisit(GeoPoint destination) {
        return baseCommand(List.of(1L)).homeVisit(4L, "Rua B, 20", destination).notes("Portão azul").build();
    }

    private void givenProfessional(ApprovalMode approval) {
        when(professionalGateway.findByIdForUpdate(PROFESSIONAL_ID)).thenReturn(Optional.of(professional(approval)));
    }

    private void givenFreeScheduleAndSave() {
        when(appointmentGateway.existsOccupyingSchedule(anyLong(), any(), any(), any())).thenReturn(false);
        when(appointmentGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void savesComboRequestedAtLocation() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(2L, 1L)))
                .thenReturn(List.of(service(1L, 60, "50"), service(2L, 30, "25")));
        givenFreeScheduleAndSave();

        Appointment appointment = useCase.execute(atLocation(List.of(2L, 1L)));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getItems()).extracting(AppointmentItem::getServiceId).containsExactly(2L, 1L);
        assertThat(appointment.getEndAt()).isEqualTo(START.plusSeconds(90 * 60L));
        assertThat(appointment.getTotalPrice()).isEqualTo(Money.of("75"));
        assertThat(appointment.getLocation().getLocationId()).isEqualTo(3L);
        assertThat(appointment.getCreatedAt()).isEqualTo(NOW);
        verify(appointmentGateway).existsOccupyingSchedule(PROFESSIONAL_ID, START, appointment.getEndAt(), NOW);
        verify(appointmentGateway).save(appointment);
    }

    @Test
    void homeVisitCalculatesDistanceFromProfessionalOrigin() {
        givenProfessional(ApprovalMode.MANUAL);
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
        givenFreeScheduleAndSave();

        Appointment appointment = useCase.execute(homeVisit(new GeoPoint(0.05, 0)));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(appointment.getLocation().getCustomerAddressId()).isEqualTo(4L);
        assertThat(appointment.getLocation().getDistanceKm()).isEqualTo(new BigDecimal("5.6"));
        assertThat(appointment.getHomeVisitFee()).isEqualTo(Money.of("20"));
        assertThat(appointment.getExpiresAt()).isEqualTo(NOW.plusSeconds(30 * 60L));
    }

    @Test
    void refusesOccupiedSchedule() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
        when(appointmentGateway.existsOccupyingSchedule(anyLong(), any(), any(), any())).thenReturn(true);
        RequestAppointmentCommand command = atLocation(List.of(1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Este horário não está mais disponível.");
        verify(appointmentGateway, never()).save(any());
    }

    @Test
    void refusesUnknownProfessional() {
        when(professionalGateway.findByIdForUpdate(PROFESSIONAL_ID)).thenReturn(Optional.empty());
        RequestAppointmentCommand command = atLocation(List.of(1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Profissional não encontrada.");
    }

    @Test
    void refusesUnknownService() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(1L, 99L))).thenReturn(List.of(service(1L, 60, "50")));
        RequestAppointmentCommand command = atLocation(List.of(1L, 99L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Serviço não encontrado.");
    }

    @Test
    void repeatedServiceReachesDomainRule() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(anyCollection())).thenReturn(List.of(service(1L, 60, "50")));
        RequestAppointmentCommand command = atLocation(List.of(1L, 1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("O serviço Serviço 1 foi escolhido mais de uma vez.");
    }

    @Test
    void requiresServiceMode() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        RequestAppointmentCommand command = baseCommand(null).build();

        assertThat(command.getServiceIds()).isEmpty();
        assertThat(command.getMode()).isNull();
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe se o atendimento é no local ou a domicílio.");
    }

    @Test
    void homeVisitRequiresCustomerCoordinates() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
        RequestAppointmentCommand command = homeVisit(null);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe a localização do endereço da cliente.");
    }

    @Test
    void startTimeIsCheckedAgainstClock() {
        useCase = new RequestAppointmentUseCase(appointmentGateway, professionalGateway, salonServiceGateway,
                clockAt(START.plusSeconds(1)));
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
        RequestAppointmentCommand command = atLocation(List.of(1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Escolha um horário no futuro.");
    }

    @Test
    void commandKeepsItsOwnCopyOfServiceIds() {
        List<Long> ids = new ArrayList<>(List.of(1L));
        RequestAppointmentCommand command = atLocation(ids);

        ids.add(2L);

        assertThat(command.getServiceIds()).containsExactly(1L);
        assertThat(command.getStartAt()).isEqualTo(Instant.parse("2026-10-13T13:00:00Z"));
        assertThat(command.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(command.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(command.getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(command.getLocationId()).isEqualTo(3L);
        assertThat(command.getAddress()).isEqualTo("Rua A, 10");
    }

    @Test
    void homeVisitCommandCarriesCustomerAddress() {
        GeoPoint destination = new GeoPoint(0.01, 0);

        RequestAppointmentCommand command = homeVisit(destination);

        assertThat(command.getMode()).isEqualTo(ServiceMode.HOME_VISIT);
        assertThat(command.getCustomerAddressId()).isEqualTo(4L);
        assertThat(command.getDestination()).isEqualTo(destination);
        assertThat(command.getNotes()).isEqualTo("Portão azul");
    }
}
