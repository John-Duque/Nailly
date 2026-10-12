package com.nailly.application.salonservice;

import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.ProcedureTypeGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalonServiceUseCasesTest {

    private static final long PROFESSIONAL_ID = 10L;
    private static final long SERVICE_ID = 5L;
    private static final ProcedureType GEL = new ProcedureType(2L, "Esmaltação em gel", true);
    private static final ProcedureType INACTIVE = new ProcedureType(3L, "Acrílico", false);
    private static final SalonServiceCommand COMMAND = new SalonServiceCommand(2L, "Gel nas mãos", "Com cutilagem",
            BodyArea.HANDS, 90, Money.of("80"), true);

    @Mock
    private SalonServiceGateway salonServiceGateway;
    @Mock
    private ProfessionalGateway professionalGateway;
    @Mock
    private ProcedureTypeGateway procedureTypeGateway;

    private void givenProfessional(boolean active) {
        Professional professional = new Professional(PROFESSIONAL_ID, 7L, "Ana", null, null,
                ApprovalPolicy.defaults(), HomeVisitPolicy.notOffered());
        if (!active) {
            professional.deactivate();
        }
        when(professionalGateway.getById(PROFESSIONAL_ID)).thenReturn(professional);
    }

    private void givenProcedureType(ProcedureType procedureType) {
        when(procedureTypeGateway.getActiveById(anyLong())).thenCallRealMethod();
        when(procedureTypeGateway.findById(procedureType.getId())).thenReturn(Optional.of(procedureType));
    }

    private SalonService givenStoredService(long ownerId) {
        SalonService service = new SalonService(SERVICE_ID, ownerId,
                new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 60, Money.of("50"), true));
        when(salonServiceGateway.getByIdAndProfessional(anyLong(), anyLong())).thenCallRealMethod();
        when(salonServiceGateway.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        return service;
    }

    private void givenSaveReturnsArgument() {
        when(salonServiceGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Cria serviço para a profissional")
    void createsServiceForProfessional() {
        givenProfessional(true);
        givenProcedureType(GEL);
        givenSaveReturnsArgument();

        SalonService service = new CreateSalonServiceUseCase(salonServiceGateway, professionalGateway,
                procedureTypeGateway).execute(PROFESSIONAL_ID, COMMAND);

        assertThat(service.getId()).isNull();
        assertThat(service.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(service.getDetails().getProcedureType()).isSameAs(GEL);
        assertThat(service.getDetails().getTitle()).isEqualTo("Gel nas mãos");
        assertThat(service.getDetails().getDescription()).isEqualTo("Com cutilagem");
        assertThat(service.getDetails().getDurationMinutes()).isEqualTo(90);
        assertThat(service.getDetails().getPrice()).isEqualTo(Money.of("80"));
        assertThat(service.getDetails().isAvailableAtHome()).isTrue();
    }

    @Test
    @DisplayName("Profissional desativada não cria serviço")
    void inactiveProfessionalCannotCreateService() {
        givenProfessional(false);
        CreateSalonServiceUseCase useCase = new CreateSalonServiceUseCase(salonServiceGateway, professionalGateway,
                procedureTypeGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, COMMAND))
                .isInstanceOf(DomainException.class)
                .hasMessage("Profissional inativa não pode cadastrar serviços.");
        verify(salonServiceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Recusa técnica desativada")
    void inactiveProcedureTypeIsRefused() {
        givenProfessional(true);
        givenProcedureType(INACTIVE);
        SalonServiceCommand command = new SalonServiceCommand(3L, "Acrílico", null, BodyArea.HANDS, 120,
                Money.of("150"), false);
        CreateSalonServiceUseCase useCase = new CreateSalonServiceUseCase(salonServiceGateway, professionalGateway,
                procedureTypeGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessage("A técnica Acrílico não está mais disponível.");
    }

    @Test
    @DisplayName("Técnica inexistente dá não encontrada")
    void unknownProcedureTypeIsNotFound() {
        givenProfessional(true);
        when(procedureTypeGateway.getActiveById(anyLong())).thenCallRealMethod();
        when(procedureTypeGateway.findById(2L)).thenReturn(Optional.empty());
        CreateSalonServiceUseCase useCase = new CreateSalonServiceUseCase(salonServiceGateway, professionalGateway,
                procedureTypeGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, COMMAND))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Técnica não encontrada.");
    }

    @Test
    @DisplayName("Edita o próprio serviço")
    void updatesOwnService() {
        givenStoredService(PROFESSIONAL_ID);
        givenProcedureType(GEL);
        givenSaveReturnsArgument();

        SalonService service = new UpdateSalonServiceUseCase(salonServiceGateway, procedureTypeGateway)
                .execute(PROFESSIONAL_ID, SERVICE_ID, COMMAND);

        assertThat(service.getId()).isEqualTo(SERVICE_ID);
        assertThat(service.getDetails().getTitle()).isEqualTo("Gel nas mãos");
        assertThat(service.getDetails().getDurationMinutes()).isEqualTo(90);
    }

    @Test
    @DisplayName("Serviço de outra profissional dá não encontrado")
    void serviceOfAnotherProfessionalIsNotFound() {
        givenStoredService(99L);
        UpdateSalonServiceUseCase useCase = new UpdateSalonServiceUseCase(salonServiceGateway, procedureTypeGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, SERVICE_ID, COMMAND))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Serviço não encontrado.");
        verify(salonServiceGateway, never()).save(any());
    }

    @Test
    @DisplayName("Desativa o próprio serviço")
    void deactivatesOwnService() {
        givenStoredService(PROFESSIONAL_ID);
        givenSaveReturnsArgument();

        SalonService service = new DeactivateSalonServiceUseCase(salonServiceGateway)
                .execute(PROFESSIONAL_ID, SERVICE_ID);

        assertThat(service.isActive()).isFalse();
    }

    @Test
    @DisplayName("Lista os serviços ativos da profissional")
    void listsActiveServicesOfProfessional() {
        List<SalonService> services = List.of(new SalonService(SERVICE_ID, PROFESSIONAL_ID,
                new ServiceDetails(GEL, "Gel", null, BodyArea.HANDS, 60, Money.of("50"), true)));
        when(salonServiceGateway.findActiveByProfessional(PROFESSIONAL_ID)).thenReturn(services);

        assertThat(new ListProfessionalServicesUseCase(salonServiceGateway).execute(PROFESSIONAL_ID))
                .isEqualTo(services);
    }

    @Test
    @DisplayName("Lista as técnicas ativas")
    void listsActiveProcedureTypes() {
        when(procedureTypeGateway.findAllActive()).thenReturn(List.of(GEL));

        assertThat(new ListProcedureTypesUseCase(procedureTypeGateway).execute()).containsExactly(GEL);
    }

    @Test
    @DisplayName("Comando expõe os dados do serviço")
    void commandExposesItsData() {
        assertThat(COMMAND.getProcedureTypeId()).isEqualTo(2L);
        assertThat(COMMAND.getTitle()).isEqualTo("Gel nas mãos");
        assertThat(COMMAND.getDescription()).isEqualTo("Com cutilagem");
        assertThat(COMMAND.getBodyArea()).isEqualTo(BodyArea.HANDS);
        assertThat(COMMAND.getDurationMinutes()).isEqualTo(90);
        assertThat(COMMAND.getPrice()).isEqualTo(Money.of("80"));
        assertThat(COMMAND.isAvailableAtHome()).isTrue();
    }
}
