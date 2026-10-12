package com.nailly.infra.security;

import com.nailly.application.user.AuthenticatedAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Emite o token de acesso. O conteúdo (claims) é lido de volta pelo {@link CurrentUser}. */
@Component
public class TokenService {

    static final String ROLE = "role";
    static final String PROFESSIONAL_ID = "professionalId";
    static final String CUSTOMER_ID = "customerId";

    private final JwtEncoder encoder;
    private final Duration expiration;
    private final Clock clock;

    public TokenService(JwtEncoder encoder,
                        @Value("${nailly.security.jwt.expiration}") Duration expiration,
                        Clock clock) {
        this.encoder = encoder;
        this.expiration = expiration;
        this.clock = clock;
    }

    public AccessToken issue(AuthenticatedAccount account) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(expiration);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(String.valueOf(account.getUserId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLE, account.getRole().name());
        if (account.getProfessionalId() != null) {
            claims.claim(PROFESSIONAL_ID, account.getProfessionalId());
        }
        if (account.getCustomerId() != null) {
            claims.claim(CUSTOMER_ID, account.getCustomerId());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new AccessToken(token, expiresAt);
    }
}
