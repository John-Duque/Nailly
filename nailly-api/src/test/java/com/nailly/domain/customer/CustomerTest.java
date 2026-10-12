package com.nailly.domain.customer;

import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Place;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    private static final Address ADDRESS = new Address("Rua B", "20", null, "Jardins", "São Paulo", "SP", "01400-000");

    @Test
    @DisplayName("Cria cliente tirando espaços do nome e do telefone")
    void createsCustomerWithNormalizedTexts() {
        Customer customer = new Customer(20L, 7L, " Ana ", " 11999990000 ");

        assertThat(customer.getId()).isEqualTo(20L);
        assertThat(customer.getUserId()).isEqualTo(7L);
        assertThat(customer.getName()).isEqualTo("Ana");
        assertThat(customer.getPhone()).isEqualTo("11999990000");
    }

    @Test
    @DisplayName("Cliente pode existir sem conta no app")
    void customerWithoutAppAccount() {
        assertThat(new Customer(null, null, "Ana", "119").getUserId()).isNull();
    }

    @Test
    @DisplayName("Exige nome e telefone")
    void requiresNameAndPhone() {
        assertThatThrownBy(() -> new Customer(null, 7L, " ", "119"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome.");
        assertThatThrownBy(() -> new Customer(null, 7L, "Ana", null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o telefone.");
    }

    @Test
    @DisplayName("Endereço pertence à cliente e pode ser editado e desativado")
    void addressBelongsToCustomerAndCanChange() {
        CustomerAddress address = new CustomerAddress(4L, 20L, new Place("Casa", ADDRESS, null));
        Place work = new Place("Trabalho", ADDRESS, new GeoPoint(-23.5, -46.6));

        address.update(work);
        address.deactivate();

        assertThat(address.getId()).isEqualTo(4L);
        assertThat(address.getCustomerId()).isEqualTo(20L);
        assertThat(address.belongsTo(20L)).isTrue();
        assertThat(address.belongsTo(21L)).isFalse();
        assertThat(address.getPlace()).isSameAs(work);
        assertThat(address.requireLocation()).isEqualTo(new GeoPoint(-23.5, -46.6));
        assertThat(address.isActive()).isFalse();
    }

    @Test
    @DisplayName("Endereço sem coordenadas não serve para domicílio")
    void addressWithoutCoordinatesCannotBeUsedForHomeVisit() {
        CustomerAddress address = new CustomerAddress(4L, 20L, new Place("Casa", ADDRESS, null));

        assertThatThrownBy(address::requireLocation)
                .isInstanceOf(DomainException.class)
                .hasMessageStartingWith("Este endereço não tem localização.");
    }

    @Test
    @DisplayName("Endereço exige os dados do lugar")
    void addressRequiresPlace() {
        assertThatThrownBy(() -> new CustomerAddress(4L, 20L, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o endereço.");
    }
}
