package com.nailly.application.appointment;

import com.nailly.domain.appointment.ServiceMode;

import java.time.Instant;
import java.util.List;

/** Dados de um novo pedido de agendamento. Monte com {@link #builder()}. */
public final class RequestAppointmentCommand {

    private final long customerId;
    private final long professionalId;
    private final List<Long> serviceIds;
    private final Instant startAt;
    private final ServiceMode mode;
    private final Long locationId;
    private final Long customerAddressId;
    private final String notes;

    private RequestAppointmentCommand(Builder builder) {
        this.customerId = builder.customerId;
        this.professionalId = builder.professionalId;
        this.serviceIds = builder.serviceIds == null ? List.of() : List.copyOf(builder.serviceIds);
        this.startAt = builder.startAt;
        this.mode = builder.mode;
        this.locationId = builder.locationId;
        this.customerAddressId = builder.customerAddressId;
        this.notes = builder.notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    /** Serviços do combo, na ordem escolhida pela cliente. */
    public List<Long> getServiceIds() {
        return serviceIds;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public ServiceMode getMode() {
        return mode;
    }

    /** Local da profissional (AT_LOCATION). */
    public Long getLocationId() {
        return locationId;
    }

    /** Endereço cadastrado da cliente (HOME_VISIT). */
    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public String getNotes() {
        return notes;
    }

    public static final class Builder {

        private long customerId;
        private long professionalId;
        private List<Long> serviceIds;
        private Instant startAt;
        private ServiceMode mode;
        private Long locationId;
        private Long customerAddressId;
        private String notes;

        private Builder() {
        }

        public Builder customerId(long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder professionalId(long professionalId) {
            this.professionalId = professionalId;
            return this;
        }

        public Builder serviceIds(List<Long> serviceIds) {
            this.serviceIds = serviceIds;
            return this;
        }

        public Builder startAt(Instant startAt) {
            this.startAt = startAt;
            return this;
        }

        /** Atendimento num local da profissional. */
        public Builder atLocation(Long locationId) {
            this.mode = ServiceMode.AT_LOCATION;
            this.locationId = locationId;
            return this;
        }

        /** Atendimento num endereço cadastrado da cliente. */
        public Builder homeVisit(Long customerAddressId) {
            this.mode = ServiceMode.HOME_VISIT;
            this.customerAddressId = customerAddressId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public RequestAppointmentCommand build() {
            return new RequestAppointmentCommand(this);
        }
    }
}
