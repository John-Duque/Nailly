package com.nailly.application.professional;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;

public class GetProfessionalUseCase {

    private final ProfessionalGateway professionalGateway;

    public GetProfessionalUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long professionalId) {
        return professionalGateway.getById(professionalId);
    }
}
