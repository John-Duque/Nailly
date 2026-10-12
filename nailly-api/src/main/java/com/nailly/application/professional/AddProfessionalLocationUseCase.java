package com.nailly.application.professional;

import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.shared.Place;

/** A profissional cadastra um local onde atende. */
public class AddProfessionalLocationUseCase {

    private final ProfessionalGateway professionalGateway;
    private final ProfessionalLocationGateway locationGateway;

    public AddProfessionalLocationUseCase(ProfessionalGateway professionalGateway,
                                          ProfessionalLocationGateway locationGateway) {
        this.professionalGateway = professionalGateway;
        this.locationGateway = locationGateway;
    }

    public ProfessionalLocation execute(long professionalId, Place place) {
        professionalGateway.getById(professionalId);
        return locationGateway.save(new ProfessionalLocation(null, professionalId, place));
    }
}
