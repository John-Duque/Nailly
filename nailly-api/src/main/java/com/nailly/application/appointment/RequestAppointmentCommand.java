package com.nailly.application.appointment;

import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.shared.GeoPoint;

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
    private final String address;
    private final GeoPoint destination;
    private final String notes;

    private RequestAppointmentCommand(Builder builder) {
        this.customerId = builder.customerId;
        this.professionalId = builder.professionalId;
        this.serviceIds = builder.serviceIds == null ? List.of() : List.copyOf(builder.serviceIds);
        this.startAt = builder.startAt;
        this.mode = builder.mode;
        this.locationId = builder.locationId;
        this.customerAddressId = builder.customerAddressId;
        this.address = builder.address;
        this.destination = builder.destination;
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

    /** Endereço da cliente (HOME_VISIT). */
    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    /** Endereço do atendimento em texto, guardado como histórico. */
    public String getAddress() {
        return address;
    }

    /** Coordenadas da cliente, usadas para calcular a distância (HOME_VISIT). */
    public GeoPoint getDestination() {
        return destination;
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
        private String address;
        private GeoPoint destination;
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

        /** Atendimento no local da profissional. */
        public Builder atLocation(Long locationId, String address) {
            this.mode = ServiceMode.AT_LOCATION;
            this.locationId = locationId;
            this.address = address;
            return this;
        }

        /** Atendimento na casa da cliente. */
        public Builder homeVisit(Long customerAddressId, String address, GeoPoint destination) {
            this.mode = ServiceMode.HOME_VISIT;
            this.customerAddressId = customerAddressId;
            this.address = address;
            this.destination = destination;
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
