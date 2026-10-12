package com.nailly.application.professional;

import com.nailly.application.user.AccountRegistration;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;

/** Cadastro da profissional: cria a conta de acesso e o perfil, com as regras padrão de aprovação. */
public class RegisterProfessionalUseCase {

    private final AccountRegistration accountRegistration;
    private final ProfessionalGateway professionalGateway;

    public RegisterProfessionalUseCase(AccountRegistration accountRegistration,
                                       ProfessionalGateway professionalGateway) {
        this.accountRegistration = accountRegistration;
        this.professionalGateway = professionalGateway;
    }

    public AuthenticatedAccount execute(String email, String password, String name, String phone, String bio) {
        User user = accountRegistration.create(email, password, Role.PROFESSIONAL);
        Professional professional = professionalGateway.save(
                Professional.register(user.getId(), name, phone, bio));
        return new AuthenticatedAccount(user.getId(), Role.PROFESSIONAL, professional.getId(), null);
    }
}
