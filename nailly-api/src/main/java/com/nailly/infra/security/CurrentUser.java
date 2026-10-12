package com.nailly.infra.security;

import com.nailly.domain.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quem está logado, lido do token. As checagens de dono (a profissional só altera os próprios
 * dados, a cliente só vê os próprios agendamentos) lançam AccessDeniedException, que vira 403.
 */
public final class CurrentUser {

    private final long userId;
    private final Role role;
    private final Long professionalId;
    private final Long customerId;

    private CurrentUser(long userId, Role role, Long professionalId, Long customerId) {
        this.userId = userId;
        this.role = role;
        this.professionalId = professionalId;
        this.customerId = customerId;
    }

    public static CurrentUser of(Jwt jwt) {
        return new CurrentUser(Long.parseLong(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString(TokenService.ROLE)),
                longClaim(jwt, TokenService.PROFESSIONAL_ID), longClaim(jwt, TokenService.CUSTOMER_ID));
    }

    private static Long longClaim(Jwt jwt, String name) {
        Object value = jwt.getClaim(name);
        return value instanceof Number number ? number.longValue() : null;
    }

    /** Id da profissional logada; recusa quem não é profissional. */
    public long professionalId() {
        if (role != Role.PROFESSIONAL || professionalId == null) {
            throw new AccessDeniedException("Acesso permitido apenas para profissionais.");
        }
        return professionalId;
    }

    /** Id da cliente logada; recusa quem não é cliente. */
    public long customerId() {
        if (role != Role.CUSTOMER || customerId == null) {
            throw new AccessDeniedException("Acesso permitido apenas para clientes.");
        }
        return customerId;
    }

    /** Garante que a profissional logada é a dona do recurso. */
    public void requireProfessional(long ownerProfessionalId) {
        if (professionalId() != ownerProfessionalId) {
            throw new AccessDeniedException("Você só pode alterar os seus próprios dados.");
        }
    }

    /** Garante que quem está logado é a cliente ou a profissional do agendamento. */
    public void requireParticipant(long appointmentCustomerId, long appointmentProfessionalId) {
        boolean isCustomer = role == Role.CUSTOMER && customerId != null && customerId == appointmentCustomerId;
        boolean isProfessional = role == Role.PROFESSIONAL && professionalId != null
                && professionalId == appointmentProfessionalId;
        if (!isCustomer && !isProfessional) {
            throw new AccessDeniedException("Você não participa deste agendamento.");
        }
    }

    public long getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }
}
