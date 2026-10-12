package com.nailly.application.salonservice;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.ProcedureTypeGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.shared.DomainException;

/** A profissional cadastra um serviço que oferece. */
public class CreateSalonServiceUseCase {

    private final SalonServiceGateway salonServiceGateway;
    private final ProfessionalGateway professionalGateway;
    private final ProcedureTypeGateway procedureTypeGateway;

    public CreateSalonServiceUseCase(SalonServiceGateway salonServiceGateway,
                                     ProfessionalGateway professionalGateway,
                                     ProcedureTypeGateway procedureTypeGateway) {
        this.salonServiceGateway = salonServiceGateway;
        this.professionalGateway = professionalGateway;
        this.procedureTypeGateway = procedureTypeGateway;
    }

    public SalonService execute(long professionalId, SalonServiceCommand command) {
        Professional professional = professionalGateway.getById(professionalId);
        if (!professional.isActive()) {
            throw new DomainException("Profissional inativa não pode cadastrar serviços.");
        }
        SalonService service = new SalonService(null, professionalId,
                command.toDetails(procedureTypeGateway.getActiveById(command.getProcedureTypeId())));
        return salonServiceGateway.save(service);
    }
}
