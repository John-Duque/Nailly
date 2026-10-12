package com.nailly.domain.appointment;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;

import java.math.BigDecimal;
import java.util.Objects;

/** Onde o atendimento acontece. */
public final class ServiceLocation {

    private final ServiceMode mode;
    private final Long locationId;
    private final Long customerAddressId;
    private final String address;
    private final BigDecimal distanceKm;

    /**
     * @param locationId        local da profissional (obrigatório em AT_LOCATION)
     * @param customerAddressId endereço da cliente (obrigatório em HOME_VISIT)
     * @param address           cópia do endereço, para o histórico não mudar
     * @param distanceKm        distância em linha reta até a cliente (obrigatória em HOME_VISIT)
     */
    public ServiceLocation(ServiceMode mode, Long locationId, Long customerAddressId, String address,
                           BigDecimal distanceKm) {
        Objects.requireNonNull(mode, "mode");
        if (mode == ServiceMode.AT_LOCATION && locationId == null) {
            throw new DomainException("Informe o local do atendimento.");
        }
        if (mode == ServiceMode.HOME_VISIT) {
            if (customerAddressId == null) {
                throw new DomainException("Informe o endereço da cliente.");
            }
            if (distanceKm == null || distanceKm.signum() < 0) {
                throw new DomainException("Distância até a cliente inválida.");
            }
            distanceKm = DomainValidation.requireScale(distanceKm, 1,
                    "A distância deve ter no máximo uma casa decimal.");
        }
        this.mode = mode;
        this.locationId = locationId;
        this.customerAddressId = customerAddressId;
        this.address = DomainValidation.requireText(address, "Informe o endereço do atendimento.");
        this.distanceKm = distanceKm;
    }

    public static ServiceLocation atLocation(long locationId, String address) {
        return new ServiceLocation(ServiceMode.AT_LOCATION, locationId, null, address, null);
    }

    public static ServiceLocation homeVisit(long customerAddressId, String address, BigDecimal distanceKm) {
        return new ServiceLocation(ServiceMode.HOME_VISIT, null, customerAddressId, address, distanceKm);
    }

    public boolean isHomeVisit() {
        return mode == ServiceMode.HOME_VISIT;
    }

    public ServiceMode getMode() {
        return mode;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Long getCustomerAddressId() {
        return customerAddressId;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }
}
