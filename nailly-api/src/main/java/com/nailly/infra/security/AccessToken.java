package com.nailly.infra.security;

import java.time.Instant;

/** Token de acesso emitido no login ou no cadastro. */
public final class AccessToken {

    private final String value;
    private final Instant expiresAt;

    public AccessToken(String value, Instant expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
    }

    public String getValue() {
        return value;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
