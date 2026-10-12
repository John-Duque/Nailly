package com.nailly.domain.customer;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;

/** Endereço da cliente, usado no atendimento a domicílio. */
public class CustomerAddress {

    private final Long id;
    private final long customerId;
    private Place place;
    private boolean active = true;

    public CustomerAddress(Long id, long customerId, Place place) {
        this.id = id;
        this.customerId = customerId;
        update(place);
    }

    public final void update(Place place) {
        if (place == null) {
            throw new DomainException("Informe o endereço.");
        }
        this.place = place;
    }

    public void deactivate() {
        active = false;
    }

    public boolean belongsTo(long customerId) {
        return this.customerId == customerId;
    }

    /** Coordenadas para calcular a distância até a profissional. */
    public GeoPoint requireLocation() {
        if (place.getLocation() == null) {
            throw new DomainException("Este endereço não tem localização. Atualize o endereço para pedir "
                    + "atendimento a domicílio.");
        }
        return place.getLocation();
    }

    public Long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public Place getPlace() {
        return place;
    }

    public boolean isActive() {
        return active;
    }
}
