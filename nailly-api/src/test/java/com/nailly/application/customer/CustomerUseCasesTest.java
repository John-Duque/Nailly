package com.nailly.application.customer;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.shared.Place;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUseCasesTest {

    private static final long CUSTOMER_ID = 20L;
    private static final long ADDRESS_ID = 4L;
    private static final Place HOME = new Place("Casa",
            new Address("Rua B", "20", null, "Jardins", "São Paulo", "SP", "01400-000"), new GeoPoint(-23.5, -46.6));

    @Mock
    private CustomerGateway customerGateway;
    @Mock
    private CustomerAddressGateway addressGateway;

    private Customer givenCustomer() {
        Customer customer = new Customer(CUSTOMER_ID, 7L, "Ana", "11999990000");
        when(customerGateway.getById(CUSTOMER_ID)).thenReturn(customer);
        return customer;
    }

    private CustomerAddress givenAddress(long ownerId) {
        CustomerAddress address = new CustomerAddress(ADDRESS_ID, ownerId, HOME);
        when(addressGateway.getByIdAndCustomer(anyLong(), anyLong())).thenCallRealMethod();
        when(addressGateway.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        return address;
    }

    private void givenSaveReturnsArgument() {
        when(addressGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getsCustomer() {
        Customer customer = givenCustomer();

        assertThat(new GetCustomerUseCase(customerGateway).execute(CUSTOMER_ID)).isSameAs(customer);
    }

    @Test
    void unknownCustomerIsNotFound() {
        when(customerGateway.getById(CUSTOMER_ID)).thenCallRealMethod();
        when(customerGateway.findById(CUSTOMER_ID)).thenReturn(Optional.empty());
        GetCustomerUseCase useCase = new GetCustomerUseCase(customerGateway);

        assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente não encontrada.");
    }

    @Test
    void updatesProfile() {
        givenCustomer();
        when(customerGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer customer = new UpdateCustomerProfileUseCase(customerGateway)
                .execute(CUSTOMER_ID, " Ana Paula ", "11988887777");

        assertThat(customer.getName()).isEqualTo("Ana Paula");
        assertThat(customer.getPhone()).isEqualTo("11988887777");
    }

    @Test
    void invalidProfileIsNotSaved() {
        givenCustomer();
        UpdateCustomerProfileUseCase useCase = new UpdateCustomerProfileUseCase(customerGateway);

        assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID, "Ana", " "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o telefone.");
        verify(customerGateway, never()).save(any());
    }

    @Test
    void addsAddressToExistingCustomer() {
        givenCustomer();
        givenSaveReturnsArgument();

        CustomerAddress address = new AddCustomerAddressUseCase(customerGateway, addressGateway)
                .execute(CUSTOMER_ID, HOME);

        assertThat(address.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(address.getPlace()).isSameAs(HOME);
        assertThat(address.isActive()).isTrue();
    }

    @Test
    void updatesOwnAddress() {
        givenAddress(CUSTOMER_ID);
        givenSaveReturnsArgument();
        Place work = new Place("Trabalho", HOME.getAddress(), null);

        CustomerAddress address = new UpdateCustomerAddressUseCase(addressGateway)
                .execute(CUSTOMER_ID, ADDRESS_ID, work);

        assertThat(address.getPlace().getName()).isEqualTo("Trabalho");
    }

    @Test
    void addressOfAnotherCustomerIsNotFound() {
        givenAddress(99L);
        UpdateCustomerAddressUseCase useCase = new UpdateCustomerAddressUseCase(addressGateway);

        assertThatThrownBy(() -> useCase.execute(CUSTOMER_ID, ADDRESS_ID, HOME))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Endereço não encontrado.");
        verify(addressGateway, never()).save(any());
    }

    @Test
    void deactivatesOwnAddress() {
        givenAddress(CUSTOMER_ID);
        givenSaveReturnsArgument();

        CustomerAddress address = new DeactivateCustomerAddressUseCase(addressGateway)
                .execute(CUSTOMER_ID, ADDRESS_ID);

        assertThat(address.isActive()).isFalse();
    }

    @Test
    void listsActiveAddresses() {
        List<CustomerAddress> addresses = List.of(new CustomerAddress(ADDRESS_ID, CUSTOMER_ID, HOME));
        when(addressGateway.findActiveByCustomer(CUSTOMER_ID)).thenReturn(addresses);

        assertThat(new ListCustomerAddressesUseCase(addressGateway).execute(CUSTOMER_ID)).isEqualTo(addresses);
    }
}
