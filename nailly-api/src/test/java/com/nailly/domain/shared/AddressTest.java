package com.nailly.domain.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    private static Address address(String complement) {
        return new Address(" Rua A ", "10", complement, "Centro", "São Paulo", "sp", "01000000");
    }

    @Test
    void normalizesStateAndZipCode() {
        Address address = address(null);

        assertThat(address.getStreet()).isEqualTo("Rua A");
        assertThat(address.getNumber()).isEqualTo("10");
        assertThat(address.getComplement()).isNull();
        assertThat(address.getNeighborhood()).isEqualTo("Centro");
        assertThat(address.getCity()).isEqualTo("São Paulo");
        assertThat(address.getState()).isEqualTo("SP");
        assertThat(address.getZipCode()).isEqualTo("01000-000");
    }

    @Test
    void formatsInOneLine() {
        assertThat(address(null).format()).isEqualTo("Rua A, 10, Centro, São Paulo/SP, 01000-000");
        assertThat(address("Apto 3")).hasToString("Rua A, 10 - Apto 3, Centro, São Paulo/SP, 01000-000");
    }

    @Test
    void formattedTextFitsTheAppointmentColumn() {
        Address longAddress = new Address("R".repeat(150), "1".repeat(20), "C".repeat(80), "B".repeat(80),
                "S".repeat(80), "SP", "01000-000");

        assertThat(longAddress.format()).hasSize(Address.MAX_TEXT_LENGTH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"S", "SPP", "S1"})
    void rejectsInvalidState(String state) {
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "Centro", "São Paulo", state, "01000-000"))
                .isInstanceOf(DomainException.class)
                .hasMessage("UF inválida.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0100-000", "010000000", "01000-00A"})
    void rejectsInvalidZipCode(String zipCode) {
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", zipCode))
                .isInstanceOf(DomainException.class)
                .hasMessage("CEP inválido.");
    }

    @Test
    void requiresMainFields() {
        assertThatThrownBy(() -> new Address(" ", "10", null, "Centro", "São Paulo", "SP", "01000-000"))
                .hasMessage("Informe a rua.");
        assertThatThrownBy(() -> new Address("Rua A", null, null, "Centro", "São Paulo", "SP", "01000-000"))
                .hasMessage("Informe o número.");
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "", "São Paulo", "SP", "01000-000"))
                .hasMessage("Informe o bairro.");
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "Centro", null, "SP", "01000-000"))
                .hasMessage("Informe a cidade.");
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "Centro", "São Paulo", null, "01000-000"))
                .hasMessage("Informe a UF.");
        assertThatThrownBy(() -> new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", " "))
                .hasMessage("Informe o CEP.");
    }

    @Test
    void placeRequiresNameAndAddress() {
        Address address = address(null);
        GeoPoint point = new GeoPoint(-23.5, -46.6);
        Place place = new Place(" Casa ", address, point);

        assertThat(place.getName()).isEqualTo("Casa");
        assertThat(place.getAddress()).isSameAs(address);
        assertThat(place.getLocation()).isSameAs(point);
        assertThatThrownBy(() -> new Place("Casa", null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço.");
        assertThatThrownBy(() -> new Place(" ", address, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome do lugar.");
    }
}
