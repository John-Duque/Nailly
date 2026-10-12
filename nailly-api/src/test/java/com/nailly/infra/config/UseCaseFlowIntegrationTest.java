package com.nailly.infra.config;

import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.UserGateway;
import com.nailly.infra.persistence.appointment.JpaAppointmentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Os casos de uso montados pelo Spring rodam em transação. Sem @Transactional no teste,
 * para a transação ser a do próprio caso de uso.
 */
@SpringBootTest
@ActiveProfiles("test")
class UseCaseFlowIntegrationTest {

    @Autowired
    private RequestAppointmentUseCase requestAppointment;
    @Autowired
    private RegisterProfessionalUseCase registerProfessional;
    @Autowired
    private UserGateway userGateway;

    @Test
    void useCasesAreWrappedInTransactions() {
        assertThat(AopUtils.isAopProxy(requestAppointment)).isTrue();
        assertThat(AopUtils.isAopProxy(registerProfessional)).isTrue();

        assertThat(UseCaseTransactionConfig.useCases().matches(RequestAppointmentUseCase.class)).isTrue();
        assertThat(UseCaseTransactionConfig.useCases().matches(RequestAppointmentCommand.class)).isFalse();
        assertThat(UseCaseTransactionConfig.useCases().matches(JpaAppointmentGateway.class)).isFalse();
    }

    @Test
    void failureRollsBackEverythingTheUseCaseWrote() {
        assertThatThrownBy(() -> registerProfessional.execute("rollback@nailly.com", "senha-forte", " ", null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome da profissional.");

        assertThat(userGateway.existsByEmail("rollback@nailly.com")).isFalse();
    }
}
