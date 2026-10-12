package com.nailly.application.professional;

import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;

/** A profissional muda como aprova os pedidos. Vale para os pedidos feitos daqui em diante. */
public class UpdateApprovalPolicyUseCase {

    private final ProfessionalGateway professionalGateway;

    public UpdateApprovalPolicyUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long professionalId, ApprovalPolicy approvalPolicy) {
        Professional professional = professionalGateway.getById(professionalId);
        professional.changeApprovalPolicy(approvalPolicy);
        return professionalGateway.save(professional);
    }
}
