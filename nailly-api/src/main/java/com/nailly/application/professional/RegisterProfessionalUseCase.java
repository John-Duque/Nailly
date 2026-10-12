package com.nailly.application.professional;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.DomainException;

/** Cria o perfil de profissional de um usuário, com as regras padrão de aprovação. */
public class RegisterProfessionalUseCase {

    private final ProfessionalGateway professionalGateway;

    public RegisterProfessionalUseCase(ProfessionalGateway professionalGateway) {
        this.professionalGateway = professionalGateway;
    }

    public Professional execute(long userId, String name, String phone, String bio) {
        if (professionalGateway.existsByUserId(userId)) {
            throw new DomainException("Este usuário já tem um perfil de profissional.");
        }
        return professionalGateway.save(Professional.register(userId, name, phone, bio));
    }
}
