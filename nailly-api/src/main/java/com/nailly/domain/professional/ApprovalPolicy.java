package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Como a profissional aprova os pedidos. */
public final class ApprovalPolicy {

    private final ApprovalMode bookingApproval;
    private final ApprovalMode homeVisitApproval;
    private final Integer requestExpirationMinutes;

    /**
     * @param bookingApproval          aprovação dos agendamentos no local dela
     * @param homeVisitApproval        aprovação dos atendimentos a domicílio
     * @param requestExpirationMinutes prazo para responder um pedido PENDING; {@code null} = não expira
     */
    public ApprovalPolicy(ApprovalMode bookingApproval, ApprovalMode homeVisitApproval,
                          Integer requestExpirationMinutes) {
        if (bookingApproval == null || homeVisitApproval == null) {
            throw new DomainException("Informe o tipo de aprovação dos agendamentos.");
        }
        if (requestExpirationMinutes != null && requestExpirationMinutes <= 0) {
            throw new DomainException("O prazo para responder um pedido deve ser maior que zero.");
        }
        this.bookingApproval = bookingApproval;
        this.homeVisitApproval = homeVisitApproval;
        this.requestExpirationMinutes = requestExpirationMinutes;
    }

    /** Padrão do banco: no local é automático, a domicílio é manual, sem prazo de resposta. */
    public static ApprovalPolicy defaults() {
        return new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, null);
    }

    /** Hora limite para responder um pedido feito em {@code requestedAt}, nunca depois da hora marcada. */
    public Optional<Instant> expiresAt(Instant requestedAt, Instant startAt) {
        if (requestExpirationMinutes == null) {
            return Optional.empty();
        }
        Instant deadline = requestedAt.plus(Duration.ofMinutes(requestExpirationMinutes));
        return Optional.of(deadline.isBefore(startAt) ? deadline : startAt);
    }

    public ApprovalMode getBookingApproval() {
        return bookingApproval;
    }

    public ApprovalMode getHomeVisitApproval() {
        return homeVisitApproval;
    }

    public Integer getRequestExpirationMinutes() {
        return requestExpirationMinutes;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ApprovalPolicy policy
                && bookingApproval == policy.bookingApproval
                && homeVisitApproval == policy.homeVisitApproval
                && Objects.equals(requestExpirationMinutes, policy.requestExpirationMinutes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingApproval, homeVisitApproval, requestExpirationMinutes);
    }
}
