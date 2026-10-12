package com.nailly.application.professional;

import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;

/** A profissional liga ou desliga o atendimento a domicílio e define o ponto de saída e as faixas de taxa. */
public class UpdateHomeVisitPolicyUseCase {

    private final ProfessionalGateway professionalGateway;

    public UpdateHomeVisitPolicyUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long professionalId, HomeVisitPolicy homeVisitPolicy) {
        Professional professional = professionalGateway.getById(professionalId);
        professional.changeHomeVisitPolicy(homeVisitPolicy);
        return professionalGateway.save(professional);
    }
}
