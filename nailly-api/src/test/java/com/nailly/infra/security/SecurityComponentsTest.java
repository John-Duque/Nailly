package com.nailly.infra.security;

import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tokens, leitura do usuário logado e hash de senha, sem subir o Spring. */
class SecurityComponentsTest {

    private static final String SECRET = "segredo-de-teste-com-pelo-menos-32-bytes";
    private static final JwtConfig JWT = new JwtConfig(SECRET);
    private static final JwtDecoder DECODER = JWT.jwtDecoder();

    private static TokenService tokensAt(Instant now) {
        return new TokenService(JWT.jwtEncoder(), Duration.ofHours(8), Clock.fixed(now, ZoneOffset.UTC));
    }

    private static Jwt tokenFor(AuthenticatedAccount account) {
        return DECODER.decode(tokensAt(Instant.now()).issue(account).getValue());
    }

    private static CurrentUser professional(long professionalId) {
        return CurrentUser.of(tokenFor(new AuthenticatedAccount(7L, Role.PROFESSIONAL, professionalId, null)));
    }

    private static CurrentUser customer(long customerId) {
        return CurrentUser.of(tokenFor(new AuthenticatedAccount(8L, Role.CUSTOMER, null, customerId)));
    }

    @Test
    @DisplayName("Emite token assinado com papel e perfil")
    void issuesSignedTokenWithProfileClaims() {
        Instant now = Instant.now();
        AccessToken token = tokensAt(now).issue(new AuthenticatedAccount(7L, Role.PROFESSIONAL, 10L, null));

        Jwt jwt = DECODER.decode(token.getValue());

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("nailly-api");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("PROFESSIONAL");
        assertThat(jwt.<Long>getClaim("professionalId")).isEqualTo(10L);
        assertThat(jwt.hasClaim("customerId")).isFalse();
        assertThat(token.getExpiresAt()).isEqualTo(now.plus(Duration.ofHours(8)));
    }

    @Test
    @DisplayName("Recusa token vencido, alterado ou de outra chave")
    void rejectsExpiredOrTamperedTokens() {
        String expired = tokensAt(Instant.now().minus(Duration.ofDays(1)))
                .issue(new AuthenticatedAccount(7L, Role.CUSTOMER, null, 20L)).getValue();
        String valid = tokensAt(Instant.now()).issue(new AuthenticatedAccount(7L, Role.CUSTOMER, null, 20L)).getValue();
        String tampered = valid.substring(0, valid.length() - 2) + (valid.endsWith("A") ? "BB" : "AA");
        JwtDecoder otherKey = new JwtConfig("outro-segredo-com-pelo-menos-32-bytes!!").jwtDecoder();

        assertThatThrownBy(() -> DECODER.decode(expired)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> DECODER.decode(tampered)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> otherKey.decode(valid)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Recusa segredo com menos de 32 bytes")
    void refusesShortSecret() {
        assertThatThrownBy(() -> new JwtConfig("curto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET deve ter pelo menos 32 bytes.");
    }

    @Test
    @DisplayName("Profissional só altera os próprios dados")
    void professionalOnlyManagesHerOwnData() {
        CurrentUser ana = professional(10L);

        assertThat(ana.getUserId()).isEqualTo(7L);
        assertThat(ana.getRole()).isEqualTo(Role.PROFESSIONAL);
        assertThat(ana.professionalId()).isEqualTo(10L);
        ana.requireProfessional(10L);
        assertThatThrownBy(() -> ana.requireProfessional(11L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Você só pode alterar os seus próprios dados.");
        assertThatThrownBy(ana::customerId)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso permitido apenas para clientes.");
    }

    @Test
    @DisplayName("Cliente não age como profissional")
    void customerCannotActAsProfessional() {
        CurrentUser bia = customer(20L);

        assertThat(bia.customerId()).isEqualTo(20L);
        assertThatThrownBy(bia::professionalId)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso permitido apenas para profissionais.");
    }

    @Test
    @DisplayName("Só os participantes veem o agendamento")
    void onlyParticipantsSeeAnAppointment() {
        professional(10L).requireParticipant(20L, 10L);
        customer(20L).requireParticipant(20L, 10L);

        assertThatThrownBy(() -> professional(11L).requireParticipant(20L, 10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Você não participa deste agendamento.");
        assertThatThrownBy(() -> customer(21L).requireParticipant(20L, 10L))
                .isInstanceOf(AccessDeniedException.class);
        CurrentUser admin = CurrentUser.of(tokenFor(new AuthenticatedAccount(1L, Role.ADMIN, null, null)));
        assertThatThrownBy(() -> admin.requireParticipant(20L, 10L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Gera e confere hash de senha com BCrypt")
    void hashesPasswordsWithBcrypt() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();

        String hash = hasher.hash("senha-forte");

        assertThat(hash).startsWith("$2").isNotEqualTo("senha-forte");
        assertThat(hasher.matches("senha-forte", hash)).isTrue();
        assertThat(hasher.matches("outra-senha", hash)).isFalse();
    }
}
