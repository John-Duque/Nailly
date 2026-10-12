package com.nailly.infra.config;

import com.nailly.application.professional.AddProfessionalLocationUseCase;
import com.nailly.application.professional.DeactivateProfessionalLocationUseCase;
import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.DefineWorkingHoursUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.ListProfessionalLocationsUseCase;
import com.nailly.application.professional.ListWorkingHoursUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalLocationUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.application.user.AccountRegistration;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHoursGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso de profissional como beans. */
@Configuration(proxyBeanMethods = false)
public class ProfessionalUseCaseConfig {

    @Bean
    public RegisterProfessionalUseCase registerProfessionalUseCase(AccountRegistration accountRegistration,
                                                                   ProfessionalGateway professionalGateway) {
        return new RegisterProfessionalUseCase(accountRegistration, professionalGateway);
    }

    @Bean
    public AddProfessionalLocationUseCase addProfessionalLocationUseCase(ProfessionalGateway professionalGateway,
                                                                         ProfessionalLocationGateway locationGateway) {
        return new AddProfessionalLocationUseCase(professionalGateway, locationGateway);
    }

    @Bean
    public UpdateProfessionalLocationUseCase updateProfessionalLocationUseCase(
            ProfessionalLocationGateway locationGateway) {
        return new UpdateProfessionalLocationUseCase(locationGateway);
    }

    @Bean
    public DeactivateProfessionalLocationUseCase deactivateProfessionalLocationUseCase(
            ProfessionalLocationGateway locationGateway) {
        return new DeactivateProfessionalLocationUseCase(locationGateway);
    }

    @Bean
    public ListProfessionalLocationsUseCase listProfessionalLocationsUseCase(
            ProfessionalLocationGateway locationGateway) {
        return new ListProfessionalLocationsUseCase(locationGateway);
    }

    @Bean
    public DefineWorkingHoursUseCase defineWorkingHoursUseCase(ProfessionalGateway professionalGateway,
                                                               ProfessionalLocationGateway locationGateway,
                                                               WorkingHoursGateway workingHoursGateway) {
        return new DefineWorkingHoursUseCase(professionalGateway, locationGateway, workingHoursGateway);
    }

    @Bean
    public ListWorkingHoursUseCase listWorkingHoursUseCase(WorkingHoursGateway workingHoursGateway) {
        return new ListWorkingHoursUseCase(workingHoursGateway);
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
