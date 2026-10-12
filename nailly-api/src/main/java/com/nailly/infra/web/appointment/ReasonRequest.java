package com.nailly.infra.web.appointment;

import jakarta.validation.constraints.Size;

/** Motivo opcional ao recusar ou cancelar um agendamento. */
public class ReasonRequest {

    @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres.")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
