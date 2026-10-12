package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalPolicyTest {

    private static final Instant REQUESTED_AT = Instant.parse("2026-10-12T12:00:00Z");

    @Test
    void doesNotExpireWithoutDeadline() {
        ApprovalPolicy policy = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, null);

        assertThat(policy.expiresAt(REQUESTED_AT, REQUESTED_AT.plusSeconds(86_400))).isEmpty();
    }

    @Test
    void expiresAfterDeadline() {
        ApprovalPolicy policy = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 60);

        assertThat(policy.expiresAt(REQUESTED_AT, REQUESTED_AT.plusSeconds(86_400)))
                .contains(Instant.parse("2026-10-12T13:00:00Z"));
    }

    @Test
    void neverExpiresAfterStartTime() {
        ApprovalPolicy policy = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 120);
        Instant startAt = Instant.parse("2026-10-12T13:00:00Z");

        assertThat(policy.expiresAt(REQUESTED_AT, startAt)).contains(startAt);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void rejectsNonPositiveDeadline(int minutes) {
        assertThatThrownBy(() -> new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, minutes))
                .isInstanceOf(DomainException.class)
                .hasMessage("O prazo para responder um pedido deve ser maior que zero.");
    }

    @Test
    void requiresBothApprovalModes() {
        assertThatThrownBy(() -> new ApprovalPolicy(null, ApprovalMode.MANUAL, null))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new ApprovalPolicy(ApprovalMode.AUTOMATIC, null, null))
                .isInstanceOf(DomainException.class);
    }
}
