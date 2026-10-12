package com.nailly.infra.web.appointment;

import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.domain.appointment.ServiceMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Pedido de agendamento da cliente logada. O endereço vem do cadastro: o local da profissional
 * (AT_LOCATION, {@code locationId}) ou um endereço da cliente (HOME_VISIT, {@code customerAddressId}).
 */
public class RequestAppointmentRequest {

    @NotNull(message = "Informe a profissional.")
    @Positive(message = "Profissional inválida.")
    private Long professionalId;

    @NotEmpty(message = "Escolha ao menos um serviço.")
    private List<@NotNull(message = "Serviço vazio.") @Positive(message = "Serviço inválido.") Long> serviceIds;

    /** Data e hora em UTC, no formato ISO-8601 (ex.: 2026-10-20T13:00:00Z). */
    @NotNull(message = "Informe a data e a hora do atendimento.")
    private Instant startAt;

    @NotNull(message = "Informe onde será o atendimento (AT_LOCATION ou HOME_VISIT).")
    private ServiceMode mode;

    @Positive(message = "Local inválido.")
    private Long locationId;

    @Positive(message = "Endereço inválido.")
    private Long customerAddressId;

    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
    private String notes;

    RequestAppointmentCommand toCommand(long customerId) {
        RequestAppointmentCommand.Builder builder = RequestAppointmentCommand.builder()
                .customerId(customerId)
                .professionalId(professionalId)
                .serviceIds(serviceIds)
                .startAt(startAt)
                .notes(notes);
        if (mode == ServiceMode.HOME_VISIT) {
            builder.homeVisit(customerAddressId);
        } else {
            builder.atLocation(locationId);
        }
        return builder.build();
    }

    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }

    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public void setMode(ServiceMode mode) {
        this.mode = mode;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setCustomerAddressId(Long customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
