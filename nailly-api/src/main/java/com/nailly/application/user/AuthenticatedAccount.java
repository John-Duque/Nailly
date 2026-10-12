package com.nailly.application.user;

import com.nailly.domain.user.Role;

/**
 * Quem acabou de se cadastrar ou de fazer login. A camada web transforma isso no token de acesso.
 * Só um dos perfis vem preenchido, conforme o papel.
 */
public final class AuthenticatedAccount {

    private final long userId;
    private final Role role;
    private final Long professionalId;
    private final Long customerId;

    public AuthenticatedAccount(long userId, Role role, Long professionalId, Long customerId) {
        this.userId = userId;
        this.role = role;
        this.professionalId = professionalId;
        this.customerId = customerId;
    }

    public long getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
