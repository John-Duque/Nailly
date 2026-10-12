package com.nailly.infra.web.shared;

import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;

/** Endereço da cliente ou local da profissional devolvido pela API. */
public class PlaceResponse {

    private final Long id;
    private final String name;
    private final String street;
    private final String number;
    private final String complement;
    private final String neighborhood;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String formatted;
    private final Double latitude;
    private final Double longitude;
    private final boolean active;

    private PlaceResponse(Long id, Place place, boolean active) {
        Address address = place.getAddress();
        GeoPoint location = place.getLocation();
        this.id = id;
        this.name = place.getName();
        this.street = address.getStreet();
        this.number = address.getNumber();
        this.complement = address.getComplement();
        this.neighborhood = address.getNeighborhood();
        this.city = address.getCity();
        this.state = address.getState();
        this.zipCode = address.getZipCode();
        this.formatted = address.format();
        this.latitude = location == null ? null : location.getLatitude();
        this.longitude = location == null ? null : location.getLongitude();
        this.active = active;
    }

    public static PlaceResponse of(Long id, Place place, boolean active) {
        return new PlaceResponse(id, place, active);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    /** Endereço em uma linha, como é copiado para o agendamento. */
    public String getFormatted() {
        return formatted;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return active;
    }
}
