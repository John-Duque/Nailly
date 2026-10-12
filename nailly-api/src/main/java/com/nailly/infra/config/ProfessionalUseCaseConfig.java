package com.nailly.infra.config;

import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.domain.professional.ProfessionalGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso de profissional como beans. */
@Configuration(proxyBeanMethods = false)
public class ProfessionalUseCaseConfig {

    @Bean
    public RegisterProfessionalUseCase registerProfessionalUseCase(ProfessionalGateway professionalGateway) {
        return new RegisterProfessionalUseCase(professionalGateway);
    }

    @Bean
    public GetProfessionalUseCase getProfessionalUseCase(ProfessionalGateway professionalGateway) {
        return new GetProfessionalUseCase(professionalGateway);
    }

    @Bean
    public UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase(ProfessionalGateway professionalGateway) {
        return new UpdateProfessionalProfileUseCase(professionalGateway);
    }

    @Bean
    public UpdateApprovalPolicyUseCase updateApprovalPolicyUseCase(ProfessionalGateway professionalGateway) {
        return new UpdateApprovalPolicyUseCase(professionalGateway);
    }

    @Bean
    public UpdateHomeVisitPolicyUseCase updateHomeVisitPolicyUseCase(ProfessionalGateway professionalGateway) {
        return new UpdateHomeVisitPolicyUseCase(professionalGateway);
    }

    @Bean
    public DeactivateProfessionalUseCase deactivateProfessionalUseCase(ProfessionalGateway professionalGateway) {
        return new DeactivateProfessionalUseCase(professionalGateway);
    }
}
