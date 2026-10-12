package com.nailly.application.salonservice;

import com.nailly.domain.salonservice.ProcedureTypeGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;

/**
 * A profissional edita um serviço dela. Agendamentos já feitos não mudam,
 * porque guardam uma cópia do serviço.
 */
public class UpdateSalonServiceUseCase {

    private final SalonServiceGateway salonServiceGateway;
    private final ProcedureTypeGateway procedureTypeGateway;

    public UpdateSalonServiceUseCase(SalonServiceGateway salonServiceGateway,
                                     ProcedureTypeGateway procedureTypeGateway) {
        this.salonServiceGateway = salonServiceGateway;
        this.procedureTypeGateway = procedureTypeGateway;
    }

    public SalonService execute(long professionalId, long serviceId, SalonServiceCommand command) {
        SalonService service = salonServiceGateway.getByIdAndProfessional(serviceId, professionalId);
        service.update(command.toDetails(procedureTypeGateway.getActiveById(command.getProcedureTypeId())));
        return salonServiceGateway.save(service);
    }
}
