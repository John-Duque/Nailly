package com.nailly.application.professional;

import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;

import java.util.List;

/** Locais ativos da profissional, para a cliente escolher onde quer ser atendida. */
public class ListProfessionalLocationsUseCase {

    private final ProfessionalLocationGateway locationGateway;

    public ListProfessionalLocationsUseCase(ProfessionalLocationGateway locationGateway) {
        this.locationGateway = locationGateway;
    }

    public List<ProfessionalLocation> execute(long professionalId) {
        return locationGateway.findActiveByProfessional(professionalId);
    }
}
