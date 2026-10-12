package com.nailly.domain.appointment;

import com.nailly.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLocationTest {

    @Test
    void atLocationKeepsLocationAndAddress() {
        ServiceLocation location = ServiceLocation.atLocation(3L, " Rua A, 10 ");

        assertThat(location.getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(location.getLocationId()).isEqualTo(3L);
        assertThat(location.getAddress()).isEqualTo("Rua A, 10");
        assertThat(location.isHomeVisit()).isFalse();
    }

    @Test
    void homeVisitNormalizesDistance() {
        ServiceLocation location = ServiceLocation.homeVisit(4L, "Rua B, 20", new BigDecimal("7"));

        assertThat(location.isHomeVisit()).isTrue();
        assertThat(location.getCustomerAddressId()).isEqualTo(4L);
        assertThat(location.getDistanceKm()).isEqualTo(new BigDecimal("7.0"));
    }

    @Test
    void requiresAddress() {
        assertThatThrownBy(() -> ServiceLocation.atLocation(3L, " "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço do atendimento.");
    }

    @Test
    void atLocationRequiresLocationId() {
        assertThatThrownBy(() -> new ServiceLocation(ServiceMode.AT_LOCATION, null, null, "Rua A", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o local do atendimento.");
    }

    @Test
    void homeVisitRequiresCustomerAddress() {
        BigDecimal distance = BigDecimal.ONE;

        assertThatThrownBy(() -> new ServiceLocation(ServiceMode.HOME_VISIT, null, null, "Rua B", distance))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço da cliente.");
    }

    @Test
    void homeVisitRequiresValidDistance() {
        BigDecimal negative = new BigDecimal("-1");
        BigDecimal tooPrecise = new BigDecimal("1.25");

        assertThatThrownBy(() -> ServiceLocation.homeVisit(4L, "Rua B", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Distância até a cliente inválida.");
        assertThatThrownBy(() -> ServiceLocation.homeVisit(4L, "Rua B", negative))
                .isInstanceOf(DomainException.class)
                .hasMessage("Distância até a cliente inválida.");
        assertThatThrownBy(() -> ServiceLocation.homeVisit(4L, "Rua B", tooPrecise))
                .isInstanceOf(DomainException.class)
                .hasMessage("A distância deve ter no máximo uma casa decimal.");
    }
}
