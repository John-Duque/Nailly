package com.nailly.infra.config;

import com.nailly.application.salonservice.CreateSalonServiceUseCase;
import com.nailly.application.salonservice.DeactivateSalonServiceUseCase;
import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import com.nailly.application.salonservice.ListProfessionalServicesUseCase;
import com.nailly.application.salonservice.UpdateSalonServiceUseCase;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.ProcedureTypeGateway;
import com.nailly.domain.salonservice.SalonServiceGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso de serviços e técnicas como beans. */
@Configuration(proxyBeanMethods = false)
public class SalonServiceUseCaseConfig {

    @Bean
    public CreateSalonServiceUseCase createSalonServiceUseCase(SalonServiceGateway salonServiceGateway,
                                                               ProfessionalGateway professionalGateway,
                                                               ProcedureTypeGateway procedureTypeGateway) {
        return new CreateSalonServiceUseCase(salonServiceGateway, professionalGateway, procedureTypeGateway);
    }

    @Bean
    public UpdateSalonServiceUseCase updateSalonServiceUseCase(SalonServiceGateway salonServiceGateway,
                                                               ProcedureTypeGateway procedureTypeGateway) {
        return new UpdateSalonServiceUseCase(salonServiceGateway, procedureTypeGateway);
    }

    @Bean
    public DeactivateSalonServiceUseCase deactivateSalonServiceUseCase(SalonServiceGateway salonServiceGateway) {
        return new DeactivateSalonServiceUseCase(salonServiceGateway);
    }

    @Bean
    public ListProfessionalServicesUseCase listProfessionalServicesUseCase(SalonServiceGateway salonServiceGateway) {
        return new ListProfessionalServicesUseCase(salonServiceGateway);
    }

    @Bean
    public ListProcedureTypesUseCase listProcedureTypesUseCase(ProcedureTypeGateway procedureTypeGateway) {
        return new ListProcedureTypesUseCase(procedureTypeGateway);
    }
}
