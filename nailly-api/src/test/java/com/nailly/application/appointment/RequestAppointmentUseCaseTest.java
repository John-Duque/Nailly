package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.nailly.application.appointment.AppointmentFixtures.ADDRESS_ID;
import static com.nailly.application.appointment.AppointmentFixtures.CUSTOMER_ID;
import static com.nailly.application.appointment.AppointmentFixtures.LOCATION_ID;
import static com.nailly.application.appointment.AppointmentFixtures.NOW;
import static com.nailly.application.appointment.AppointmentFixtures.PROFESSIONAL_ID;
import static com.nailly.application.appointment.AppointmentFixtures.START;
import static com.nailly.application.appointment.AppointmentFixtures.clockAt;
import static com.nailly.application.appointment.AppointmentFixtures.customerAddress;
import static com.nailly.application.appointment.AppointmentFixtures.location;
import static com.nailly.application.appointment.AppointmentFixtures.openTuesday;
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
    @Mock
    private ProfessionalLocationGateway locationGateway;
    @Mock
    private CustomerAddressGateway customerAddressGateway;
    @Mock
    private WorkingHoursGateway workingHoursGateway;

    private RequestAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestAppointmentUseCase(appointmentGateway, professionalGateway, salonServiceGateway,
                locationGateway, customerAddressGateway, workingHoursGateway, clockAt(NOW));
    }

    private static RequestAppointmentCommand.Builder baseCommand(List<Long> serviceIds) {
        return RequestAppointmentCommand.builder()
                .customerId(CUSTOMER_ID)
                .professionalId(PROFESSIONAL_ID)
                .serviceIds(serviceIds)
                .startAt(START);
    }

    private static RequestAppointmentCommand atLocation(List<Long> serviceIds) {
        return baseCommand(serviceIds).atLocation(LOCATION_ID).build();
    }

    private static RequestAppointmentCommand homeVisit() {
        return baseCommand(List.of(1L)).homeVisit(ADDRESS_ID).notes("Portão azul").build();
    }

    private void givenProfessional(ApprovalMode approval) {
        when(professionalGateway.findByIdForUpdate(PROFESSIONAL_ID)).thenReturn(Optional.of(professional(approval)));
    }

    private void givenService() {
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
    }

    private void givenLocation(ProfessionalLocation location) {
        when(locationGateway.getByIdAndProfessional(LOCATION_ID, PROFESSIONAL_ID)).thenReturn(location);
    }

    private void givenAddress(CustomerAddress address) {
        when(customerAddressGateway.getByIdAndCustomer(ADDRESS_ID, CUSTOMER_ID)).thenReturn(address);
    }

    private void givenOpen(Long locationId) {
        when(workingHoursGateway.find(PROFESSIONAL_ID, locationId)).thenReturn(openTuesday(locationId));
    }

    private void givenFreeScheduleAndSave() {
        when(appointmentGateway.existsOccupyingSchedule(anyLong(), any(), any(), any())).thenReturn(false);
        when(appointmentGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void savesComboAtProfessionalLocation() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(2L, 1L)))
                .thenReturn(List.of(service(1L, 60, "50"), service(2L, 30, "25")));
        givenLocation(location());
        givenOpen(LOCATION_ID);
        givenFreeScheduleAndSave();

        Appointment appointment = useCase.execute(atLocation(List.of(2L, 1L)));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getItems()).extracting(AppointmentItem::getServiceId).containsExactly(2L, 1L);
        assertThat(appointment.getEndAt()).isEqualTo(START.plusSeconds(90 * 60L));
        assertThat(appointment.getTotalPrice()).isEqualTo(Money.of("75"));
        assertThat(appointment.getLocation().getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(appointment.getLocation().getAddress()).isEqualTo("Rua A, 10, Centro, São Paulo/SP, 01000-000");
        verify(appointmentGateway).existsOccupyingSchedule(PROFESSIONAL_ID, START, appointment.getEndAt(), NOW);
        verify(appointmentGateway).save(appointment);
    }

    @Test
    void homeVisitUsesRegisteredCustomerAddress() {
        givenProfessional(ApprovalMode.MANUAL);
        givenService();
        givenAddress(customerAddress(new GeoPoint(0.05, 0)));
        givenOpen(null);
        givenFreeScheduleAndSave();

        Appointment appointment = useCase.execute(homeVisit());

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(appointment.getLocation().getMode()).isEqualTo(ServiceMode.HOME_VISIT);
        assertThat(appointment.getLocation().getCustomerAddressId()).isEqualTo(ADDRESS_ID);
        assertThat(appointment.getLocation().getAddress())
                .isEqualTo("Rua B, 20 - Apto 3, Jardins, São Paulo/SP, 01400-000");
        assertThat(appointment.getLocation().getDistanceKm()).isEqualTo(new BigDecimal("5.6"));
        assertThat(appointment.getHomeVisitFee()).isEqualTo(Money.of("20"));
        assertThat(appointment.getNotes()).isEqualTo("Portão azul");
    }

    @Test
    void homeVisitNeedsAddressWithCoordinates() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        givenService();
        givenAddress(customerAddress(null));
        RequestAppointmentCommand command = homeVisit();

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageStartingWith("Este endereço não tem localização.");
    }

    @Test
    void removedAddressIsRefused() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        givenService();
        CustomerAddress removed = customerAddress(new GeoPoint(0.05, 0));
        removed.deactivate();
        givenAddress(removed);
        RequestAppointmentCommand command = homeVisit();

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Este endereço foi removido.");
    }

    @Test
    void inactiveLocationIsRefused() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        givenService();
        ProfessionalLocation closed = location();
        closed.deactivate();
        givenLocation(closed);
        RequestAppointmentCommand command = atLocation(List.of(1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Este local não está mais disponível.");
    }

    @Test
    void requiresLocationOrAddressForTheChosenMode() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        when(salonServiceGateway.findAllByIds(List.of(1L))).thenReturn(List.of(service(1L, 60, "50")));
        RequestAppointmentCommand noLocation = baseCommand(List.of(1L)).atLocation(null).build();
        RequestAppointmentCommand noAddress = baseCommand(List.of(1L)).homeVisit(null).build();

        assertThatThrownBy(() -> useCase.execute(noLocation))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o local do atendimento.");
        assertThatThrownBy(() -> useCase.execute(noAddress))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço da cliente.");
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
    void refusesTimeOutsideWorkingHours() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        givenService();
        givenLocation(location());
        when(workingHoursGateway.find(PROFESSIONAL_ID, LOCATION_ID))
                .thenReturn(new WorkingHours(PROFESSIONAL_ID, LOCATION_ID, List.of()));
        RequestAppointmentCommand command = atLocation(List.of(1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("O horário escolhido está fora do horário de atendimento.");
        verify(appointmentGateway, never()).save(any());
    }

    @Test
    void refusesOccupiedSchedule() {
        givenProfessional(ApprovalMode.AUTOMATIC);
        givenService();
        givenLocation(location());
        givenOpen(LOCATION_ID);
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
        givenLocation(location());
        RequestAppointmentCommand command = atLocation(List.of(1L, 1L));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("O serviço Serviço 1 foi escolhido mais de uma vez.");
    }

    @Test
    void commandKeepsItsOwnCopyOfServiceIds() {
        List<Long> ids = new ArrayList<>(List.of(1L));
        RequestAppointmentCommand command = atLocation(ids);

        ids.add(2L);

        assertThat(command.getServiceIds()).containsExactly(1L);
        assertThat(command.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(command.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(command.getStartAt()).isEqualTo(START);
        assertThat(command.getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(homeVisit().getCustomerAddressId()).isEqualTo(ADDRESS_ID);
    }
}
