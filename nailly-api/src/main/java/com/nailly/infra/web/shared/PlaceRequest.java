package com.nailly.infra.web.shared;

import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Lugar com endereço: o endereço da cliente ("Casa") ou o local da profissional ("Estúdio"). */
public class PlaceRequest {

    @NotBlank(message = "Informe o nome do lugar.")
    @Size(max = 60, message = "O nome do lugar deve ter no máximo 60 caracteres.")
    private String name;

    @NotBlank(message = "Informe a rua.")
    @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres.")
    private String street;

    @NotBlank(message = "Informe o número.")
    @Size(max = 20, message = "O número deve ter no máximo 20 caracteres.")
    private String number;

    @Size(max = 80, message = "O complemento deve ter no máximo 80 caracteres.")
    private String complement;

    @NotBlank(message = "Informe o bairro.")
    @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres.")
    private String neighborhood;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
    private String city;

    @NotBlank(message = "Informe a UF.")
    @Pattern(regexp = "[A-Za-z]{2}", message = "UF inválida.")
    private String state;

    @NotBlank(message = "Informe o CEP.")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.")
    private String zipCode;

    @DecimalMin(value = "-90", message = "Latitude inválida.")
    @DecimalMax(value = "90", message = "Latitude inválida.")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude inválida.")
    @DecimalMax(value = "180", message = "Longitude inválida.")
    private Double longitude;

    /** Coordenadas vêm juntas ou não vêm. */
    @AssertTrue(message = "Informe latitude e longitude juntas.")
    public boolean isCoordinatesComplete() {
        return (latitude == null) == (longitude == null);
    }

    public Place toPlace() {
        GeoPoint location = latitude == null ? null : new GeoPoint(latitude, longitude);
        return new Place(name, new Address(street, number, complement, neighborhood, city, state, zipCode), location);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
