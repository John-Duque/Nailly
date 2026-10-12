package com.nailly.infra.config;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.salonservice.CreateSalonServiceUseCase;
import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import com.nailly.application.salonservice.SalonServiceCommand;
import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Money;
import com.nailly.infra.persistence.appointment.JpaAppointmentGateway;
import com.nailly.infra.persistence.salonservice.ProcedureTypeEntity;
import com.nailly.infra.persistence.salonservice.ProcedureTypeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.nailly.infra.persistence.PersistenceTestData.CUSTOMER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.LOCATION_ID;
import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Os casos de uso montados pelo Spring, com os gateways JPA de verdade. */
@SpringBootTest
@Transactional
class UseCaseFlowIntegrationTest {

    @Autowired
    private RegisterProfessionalUseCase registerProfessional;
    @Autowired
    private CreateSalonServiceUseCase createSalonService;
    @Autowired
    private ListProcedureTypesUseCase listProcedureTypes;
    @Autowired
    private RequestAppointmentUseCase requestAppointment;
    @Autowired
    private CancelAppointmentUseCase cancelAppointment;
    @Autowired
    private GetAppointmentUseCase getAppointment;
    @Autowired
    private ProcedureTypeJpaRepository procedureTypeRepository;

    @Test
    void useCasesRunInsideTransactions() {
        assertThat(AopUtils.isAopProxy(requestAppointment)).isTrue();
        assertThat(AopUtils.isAopProxy(registerProfessional)).isTrue();

        assertThat(UseCaseTransactionConfig.useCases().matches(RequestAppointmentUseCase.class)).isTrue();
        assertThat(UseCaseTransactionConfig.useCases().matches(RequestAppointmentCommand.class)).isFalse();
        assertThat(UseCaseTransactionConfig.useCases().matches(JpaAppointmentGateway.class)).isFalse();
    }

    @Test
    void professionalRegistersServiceAndCustomerBooksIt() {
        procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true));
        long procedureTypeId = listProcedureTypes.execute().stream()
                .filter(type -> type.getName().equals("Teste gel"))
                .findFirst().orElseThrow().getId();
        Professional professional = registerProfessional.execute(USER_ID, "Ana", null, null);
        SalonService service = createSalonService.execute(professional.getId(), new SalonServiceCommand(
                procedureTypeId, "Gel nas mãos", null, BodyArea.HANDS, 60, Money.of("50"), true));
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        RequestAppointmentCommand command = RequestAppointmentCommand.builder()
                .customerId(CUSTOMER_ID)
                .professionalId(professional.getId())
                .serviceIds(List.of(service.getId()))
                .startAt(tomorrow)
                .atLocation(LOCATION_ID, "Rua A, 10")
                .build();

        Appointment appointment = requestAppointment.execute(command);

        assertThat(appointment.getId()).isNotNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(getAppointment.execute(appointment.getId()).getTotalPrice()).isEqualTo(Money.of("50"));

        assertThatThrownBy(() -> requestAppointment.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Este horário não está mais disponível.");

        cancelAppointment.execute(appointment.getId(), "Imprevisto");

        assertThat(requestAppointment.execute(command).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }
}
