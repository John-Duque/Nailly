package com.nailly.application.professional;

import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;

/** A profissional deixa de atender num local. Fica desativado, porque agendamentos antigos apontam para ele. */
public class DeactivateProfessionalLocationUseCase {

    private final ProfessionalLocationGateway locationGateway;

    public DeactivateProfessionalLocationUseCase(ProfessionalLocationGateway locationGateway) {
        this.locationGateway = locationGateway;
    }

    public ProfessionalLocation execute(long professionalId, long locationId) {
        ProfessionalLocation location = locationGateway.getByIdAndProfessional(locationId, professionalId);
        location.deactivate();
        return locationGateway.save(location);
    }
}
