package com.nailly.domain.shared;

/**
 * Lugar com nome, endereço e, opcionalmente, coordenadas.
 * Usado no endereço da cliente ("Casa") e no local de atendimento da profissional ("Estúdio").
 */
public final class Place {

    private final String name;
    private final Address address;
    private final GeoPoint location;

    /** @param location coordenadas; sem elas o lugar não serve para calcular distância */
    public Place(String name, Address address, GeoPoint location) {
        if (address == null) {
            throw new DomainException("Informe o endereço.");
        }
        this.name = DomainValidation.requireText(name, "Informe o nome do lugar.");
        this.address = address;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public GeoPoint getLocation() {
        return location;
    }
}
