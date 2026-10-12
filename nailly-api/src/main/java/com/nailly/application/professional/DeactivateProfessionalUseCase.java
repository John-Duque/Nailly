package com.nailly.application.professional;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;

/**
 * A profissional deixa de receber novos agendamentos. O registro continua no banco,
 * porque os agendamentos antigos apontam para ela.
 */
public class DeactivateProfessionalUseCase {

    private final ProfessionalGateway professionalGateway;

    public DeactivateProfessionalUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long professionalId) {
        Professional professional = professionalGateway.getById(professionalId);
        professional.deactivate();
        return professionalGateway.save(professional);
    }
}
