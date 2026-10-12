package com.nailly.domain.appointment;

import com.nailly.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLocationTest {

    @Test
    @DisplayName("No local guarda o local e o endereço sem espaços extras")
    void atLocationKeepsLocationAndAddress() {
        ServiceLocation location = ServiceLocation.atLocation(3L, " Rua A, 10 ");

        assertThat(location.getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(location.getLocationId()).isEqualTo(3L);
        assertThat(location.getAddress()).isEqualTo("Rua A, 10");
        assertThat(location.isHomeVisit()).isFalse();
    }

    @Test
    @DisplayName("A domicílio guarda o endereço da cliente e normaliza a distância")
    void homeVisitNormalizesDistance() {
        ServiceLocation location = ServiceLocation.homeVisit(4L, "Rua B, 20", new BigDecimal("7"));

        assertThat(location.isHomeVisit()).isTrue();
        assertThat(location.getCustomerAddressId()).isEqualTo(4L);
        assertThat(location.getDistanceKm()).isEqualTo(new BigDecimal("7.0"));
    }

    @Test
    @DisplayName("Exige o endereço do atendimento")
    void requiresAddress() {
        assertThatThrownBy(() -> ServiceLocation.atLocation(3L, " "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço do atendimento.");
    }

    @Test
    @DisplayName("No local exige o local da profissional")
    void atLocationRequiresLocationId() {
        assertThatThrownBy(() -> new ServiceLocation(ServiceMode.AT_LOCATION, null, null, "Rua A", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o local do atendimento.");
    }

    @Test
    @DisplayName("A domicílio exige o endereço da cliente")
    void homeVisitRequiresCustomerAddress() {
        BigDecimal distance = BigDecimal.ONE;

        assertThatThrownBy(() -> new ServiceLocation(ServiceMode.HOME_VISIT, null, null, "Rua B", distance))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço da cliente.");
    }

    @Test
    @DisplayName("A domicílio exige distância válida com até uma casa decimal")
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
