package com.nailly.application.user;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.PasswordHasher;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;
import com.nailly.domain.user.UserGateway;

/**
 * Cria a conta de acesso. Usado pelos casos de uso de cadastro de cliente e de profissional,
 * dentro da transação deles: se o perfil falhar, a conta também é desfeita.
 */
public class AccountRegistration {

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;

    public AccountRegistration(UserGateway userGateway, PasswordHasher passwordHasher) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
    }

    public User create(String email, String password, Role role) {
        if (userGateway.existsByEmail(User.normalizeEmail(email))) {
            throw new DomainException("Este e-mail já está cadastrado.");
        }
        return userGateway.save(User.register(email, password, role, passwordHasher));
    }
}
