package com.nailly.infra.web.professional;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Como a profissional aprova os pedidos no local e a domicílio. */
public class ApprovalPolicyRequest {

    @NotNull(message = "Informe a aprovação dos agendamentos no local (AUTOMATIC ou MANUAL).")
    private ApprovalMode bookingApproval;

    @NotNull(message = "Informe a aprovação dos atendimentos a domicílio (AUTOMATIC ou MANUAL).")
    private ApprovalMode homeVisitApproval;

    /** Vazio = o pedido não expira. */
    @Positive(message = "O prazo para responder deve ser maior que zero.")
    private Integer requestExpirationMinutes;

    ApprovalPolicy toDomain() {
        return new ApprovalPolicy(bookingApproval, homeVisitApproval, requestExpirationMinutes);
    }

    public void setBookingApproval(ApprovalMode bookingApproval) {
        this.bookingApproval = bookingApproval;
    }

    public void setHomeVisitApproval(ApprovalMode homeVisitApproval) {
        this.homeVisitApproval = homeVisitApproval;
    }

    public void setRequestExpirationMinutes(Integer requestExpirationMinutes) {
        this.requestExpirationMinutes = requestExpirationMinutes;
    }
}
