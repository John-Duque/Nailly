package com.nailly.application.professional;

import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.shared.Place;

/** A profissional edita um local dela. Agendamentos já feitos guardam a cópia antiga do endereço. */
public class UpdateProfessionalLocationUseCase {

    private final ProfessionalLocationGateway locationGateway;

    public UpdateProfessionalLocationUseCase(ProfessionalLocationGateway locationGateway) {
        this.locationGateway = locationGateway;
    }

    public ProfessionalLocation execute(long professionalId, long locationId, Place place) {
        ProfessionalLocation location = locationGateway.getByIdAndProfessional(locationId, professionalId);
        location.update(place);
        return locationGateway.save(location);
    }
}
