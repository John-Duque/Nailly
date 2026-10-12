package com.nailly.infra.persistence.shared;

import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Colunas de endereço e coordenadas que se repetem em customer_addresses e professional_locations.
 * O nome do lugar fica na entidade, porque a coluna muda (label ou name).
 */
@Embeddable
public class AddressColumns {

    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "complement", length = 80)
    private String complement;

    @Column(name = "neighborhood", nullable = false, length = 80)
    private String neighborhood;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "state", nullable = false, length = 2, columnDefinition = "char(2)")
    private String state;

    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    protected AddressColumns() {
    }

    public static AddressColumns from(Place place) {
        Address address = place.getAddress();
        GeoPoint location = place.getLocation();
        AddressColumns columns = new AddressColumns();
        columns.street = address.getStreet();
        columns.number = address.getNumber();
        columns.complement = address.getComplement();
        columns.neighborhood = address.getNeighborhood();
        columns.city = address.getCity();
        columns.state = address.getState();
        columns.zipCode = address.getZipCode();
        columns.latitude = location == null ? null : coordinate(location.getLatitude());
        columns.longitude = location == null ? null : coordinate(location.getLongitude());
        return columns;
    }

    public Place toPlace(String name) {
        Address address = new Address(street, number, complement, neighborhood, city, state, zipCode);
        GeoPoint location = latitude == null || longitude == null
                ? null
                : new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
        return new Place(name, address, location);
    }

    /** DECIMAL(9,6) no banco. */
    private static BigDecimal coordinate(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }
}
