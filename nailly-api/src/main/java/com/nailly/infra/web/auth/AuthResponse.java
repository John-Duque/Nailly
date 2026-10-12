package com.nailly.infra.web.auth;

import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.user.Role;
import com.nailly.infra.security.AccessToken;

import java.time.Instant;

/** Token de acesso e o perfil a que ele dá acesso. Envie o token como "Authorization: Bearer <token>". */
public class AuthResponse {

    private final String accessToken;
    private final Instant expiresAt;
    private final long userId;
    private final Role role;
    private final Long professionalId;
    private final Long customerId;

    private AuthResponse(AccessToken token, AuthenticatedAccount account) {
        this.accessToken = token.getValue();
        this.expiresAt = token.getExpiresAt();
        this.userId = account.getUserId();
        this.role = account.getRole();
        this.professionalId = account.getProfessionalId();
        this.customerId = account.getCustomerId();
    }

    public static AuthResponse of(AccessToken token, AuthenticatedAccount account) {
        return new AuthResponse(token, account);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return "Bearer";
    }

    public Instant getExpiresAt() {
        return expiresAt;
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
