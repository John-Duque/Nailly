package com.nailly.infra.web.appointment;

import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.shared.GeoPoint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Pedido de agendamento. Enquanto não existem os cadastros de cliente e de locais,
 * o endereço e as coordenadas da cliente vêm no corpo da requisição.
 */
public class RequestAppointmentRequest {

    @NotNull(message = "Informe a cliente.")
    @Positive(message = "Cliente inválida.")
    private Long customerId;

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

    @Positive(message = "Endereço da cliente inválido.")
    private Long customerAddressId;

    @NotBlank(message = "Informe o endereço do atendimento.")
    @Size(max = 400, message = "O endereço deve ter no máximo 400 caracteres.")
    private String address;

    @DecimalMin(value = "-90", message = "Latitude inválida.")
    @DecimalMax(value = "90", message = "Latitude inválida.")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude inválida.")
    @DecimalMax(value = "180", message = "Longitude inválida.")
    private Double longitude;

    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
    private String notes;

    RequestAppointmentCommand toCommand() {
        RequestAppointmentCommand.Builder builder = RequestAppointmentCommand.builder()
                .customerId(customerId)
                .professionalId(professionalId)
                .serviceIds(serviceIds)
                .startAt(startAt)
                .notes(notes);
        if (mode == ServiceMode.HOME_VISIT) {
            GeoPoint destination = latitude == null || longitude == null ? null : new GeoPoint(latitude, longitude);
            builder.homeVisit(customerAddressId, address, destination);
        } else {
            builder.atLocation(locationId, address);
        }
        return builder.build();
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
