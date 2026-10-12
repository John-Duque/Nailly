package com.nailly.application.user;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.InvalidCredentialsException;
import com.nailly.domain.user.PasswordHasher;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;
import com.nailly.domain.user.UserGateway;

import java.util.Optional;

/** Login com e-mail e senha. Qualquer falha responde a mesma mensagem, para não revelar quais e-mails existem. */
public class AuthenticateUseCase {

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;
    private final ProfessionalGateway professionalGateway;
    private final CustomerGateway customerGateway;

    public AuthenticateUseCase(UserGateway userGateway, PasswordHasher passwordHasher,
                               ProfessionalGateway professionalGateway, CustomerGateway customerGateway) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
        this.professionalGateway = professionalGateway;
        this.customerGateway = customerGateway;
    }

    public AuthenticatedAccount execute(String email, String password) {
        User user = findByEmail(email)
                .filter(User::isActive)
                .filter(candidate -> candidate.passwordMatches(password, passwordHasher))
                .orElseThrow(InvalidCredentialsException::new);
        long userId = user.getId();
        Long professionalId = user.getRole() == Role.PROFESSIONAL
                ? professionalGateway.findByUserId(userId).map(Professional::getId).orElse(null)
                : null;
        Long customerId = user.getRole() == Role.CUSTOMER
                ? customerGateway.findByUserId(userId).map(Customer::getId).orElse(null)
                : null;
        return new AuthenticatedAccount(userId, user.getRole(), professionalId, customerId);
    }

    private Optional<User> findByEmail(String email) {
        try {
            return userGateway.findByEmail(User.normalizeEmail(email));
        } catch (DomainException invalidEmail) {
            return Optional.empty();
        }
    }
}
