package com.nailly.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class GeoPointTest {

    @Test
    void distanceToSamePointIsZero() {
        GeoPoint point = new GeoPoint(-23.55, -46.63);

        assertThat(point.distanceKmTo(point)).isZero();
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        double distance = new GeoPoint(0, 0).distanceKmTo(new GeoPoint(1, 0));

        assertThat(distance).isCloseTo(111.19, within(0.01));
    }

    @Test
    void distanceIsSymmetric() {
        GeoPoint se = new GeoPoint(-23.550520, -46.633308);
        GeoPoint paulista = new GeoPoint(-23.561414, -46.655881);

        assertThat(se.distanceKmTo(paulista))
                .isCloseTo(paulista.distanceKmTo(se), within(1e-9))
                .isCloseTo(2.6, within(0.1));
    }

    @ParameterizedTest
    @CsvSource({"90.1, 0", "-90.1, 0", "NaN, 0"})
    void rejectsInvalidLatitude(double latitude, double longitude) {
        assertThatThrownBy(() -> new GeoPoint(latitude, longitude))
                .isInstanceOf(DomainException.class)
                .hasMessage("Latitude inválida.");
    }

    @ParameterizedTest
    @CsvSource({"0, 180.1", "0, -180.1", "0, NaN"})
    void rejectsInvalidLongitude(double latitude, double longitude) {
        assertThatThrownBy(() -> new GeoPoint(latitude, longitude))
                .isInstanceOf(DomainException.class)
                .hasMessage("Longitude inválida.");
    }
}
