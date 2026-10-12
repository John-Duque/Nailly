package com.nailly.infra.persistence.customer;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.shared.Place;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;
import com.nailly.domain.user.UserGateway;
import com.nailly.infra.persistence.user.JpaUserGateway;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Usuários, clientes e endereços das clientes no banco. */
@DataJpaTest
@ActiveProfiles("test")
@Import({JpaUserGateway.class, JpaCustomerGateway.class, JpaCustomerAddressGateway.class})
class JpaAccountGatewaysTest {

    private static final Address ADDRESS = new Address("Rua B", "20", "Apto 3", "Jardins", "São Paulo", "SP",
            "01400-000");

    @Autowired
    private UserGateway userGateway;
    @Autowired
    private CustomerGateway customerGateway;
    @Autowired
    private CustomerAddressGateway addressGateway;
    @Autowired
    private EntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Customer newCustomer() {
        User user = userGateway.save(new User(null, "cliente@nailly.com", "hash", Role.CUSTOMER));
        return customerGateway.save(new Customer(null, user.getId(), "Ana", "11999990000"));
    }

    @Test
    @DisplayName("Grava usuário e encontra pelo e-mail")
    void savesAndFindsUserByEmail() {
        User saved = userGateway.save(new User(null, "Ana@Nailly.com", "hash-bcrypt", Role.PROFESSIONAL));
        flushAndClear();

        User loaded = userGateway.findByEmail("ana@nailly.com").orElseThrow();

        assertThat(loaded.getId()).isEqualTo(saved.getId());
        assertThat(loaded.getPasswordHash()).isEqualTo("hash-bcrypt");
        assertThat(loaded.getRole()).isEqualTo(Role.PROFESSIONAL);
        assertThat(loaded.isActive()).isTrue();
        assertThat(userGateway.existsByEmail("ana@nailly.com")).isTrue();
        assertThat(userGateway.existsByEmail("outra@nailly.com")).isFalse();
        assertThat(userGateway.findByEmail("outra@nailly.com")).isEmpty();
    }

    @Test
    @DisplayName("Grava a desativação do usuário")
    void savesUserDeactivation() {
        User user = userGateway.save(new User(null, "ana@nailly.com", "hash", Role.CUSTOMER));
        user.deactivate();

        userGateway.save(user);
        flushAndClear();

        assertThat(userGateway.findByEmail("ana@nailly.com").orElseThrow().isActive()).isFalse();
    }

    @Test
    @DisplayName("Salvar usuário inexistente dá não encontrado")
    void savingUnknownUserIsNotFound() {
        User ghost = new User(Long.MAX_VALUE, "ana@nailly.com", "hash", Role.CUSTOMER);

        assertThatThrownBy(() -> userGateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuário não encontrado.");
    }

    @Test
    @DisplayName("Grava cliente e encontra por id e por usuário")
    void savesAndFindsCustomer() {
        Customer customer = newCustomer();
        customer.updateProfile("Ana Paula", "11988887777");
        customerGateway.save(customer);
        flushAndClear();

        Customer loaded = customerGateway.findById(customer.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Ana Paula");
        assertThat(loaded.getPhone()).isEqualTo("11988887777");
        assertThat(customerGateway.findByUserId(loaded.getUserId())).get()
                .extracting(Customer::getId).isEqualTo(customer.getId());
        assertThat(customerGateway.findByUserId(Long.MAX_VALUE)).isEmpty();
        assertThat(customerGateway.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("Salvar cliente inexistente dá não encontrada")
    void savingUnknownCustomerIsNotFound() {
        Customer ghost = new Customer(Long.MAX_VALUE, null, "Ana", "119");

        assertThatThrownBy(() -> customerGateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente não encontrada.");
    }

    @Test
    @DisplayName("Grava endereço com coordenadas")
    void savesAddressWithCoordinates() {
        Customer customer = newCustomer();
        CustomerAddress saved = addressGateway.save(new CustomerAddress(null, customer.getId(),
                new Place("Casa", ADDRESS, new GeoPoint(-23.561414, -46.655881))));
        flushAndClear();

        CustomerAddress loaded = addressGateway.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getCustomerId()).isEqualTo(customer.getId());
        assertThat(loaded.getPlace().getName()).isEqualTo("Casa");
        assertThat(loaded.getPlace().getAddress()).isEqualTo(ADDRESS);
        assertThat(loaded.getPlace().getLocation()).isEqualTo(new GeoPoint(-23.561414, -46.655881));
        assertThat(loaded.isActive()).isTrue();
    }

    @Test
    @DisplayName("Edita e desativa endereço, listando só os ativos")
    void updatesAndDeactivatesAddress() {
        Customer customer = newCustomer();
        CustomerAddress address = addressGateway.save(new CustomerAddress(null, customer.getId(),
                new Place("Casa", ADDRESS, new GeoPoint(-23.5, -46.6))));
        CustomerAddress work = addressGateway.save(new CustomerAddress(null, customer.getId(),
                new Place("Trabalho", ADDRESS, null)));
        address.update(new Place("Casa da mãe", ADDRESS, null));
        address.deactivate();

        addressGateway.save(address);
        flushAndClear();

        CustomerAddress loaded = addressGateway.findById(address.getId()).orElseThrow();
        assertThat(loaded.getPlace().getName()).isEqualTo("Casa da mãe");
        assertThat(loaded.getPlace().getLocation()).isNull();
        assertThat(loaded.isActive()).isFalse();
        assertThat(addressGateway.findActiveByCustomer(customer.getId()))
                .extracting(CustomerAddress::getId)
                .containsExactly(work.getId());
    }

    @Test
    @DisplayName("Endereço inexistente não é encontrado nem salvo")
    void unknownAddressIsEmptyAndCannotBeSaved() {
        CustomerAddress ghost = new CustomerAddress(Long.MAX_VALUE, 1L, new Place("Casa", ADDRESS, null));

        assertThat(addressGateway.findById(Long.MAX_VALUE)).isEmpty();
        assertThatThrownBy(() -> addressGateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Endereço não encontrado.");
    }
}
