package com.nailly.application.professional;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;

/** A profissional edita nome, telefone e apresentação. */
public class UpdateProfessionalProfileUseCase {

    private final ProfessionalGateway professionalGateway;

    public UpdateProfessionalProfileUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long professionalId, String name, String phone, String bio) {
        Professional professional = professionalGateway.getById(professionalId);
        professional.updateProfile(name, phone, bio);
        return professionalGateway.save(professional);
    }
}
