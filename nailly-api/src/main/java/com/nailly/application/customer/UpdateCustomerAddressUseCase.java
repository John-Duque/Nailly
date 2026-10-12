package com.nailly.application.customer;

import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.shared.Place;

/** A cliente edita um endereço dela. Agendamentos já feitos guardam a cópia antiga. */
public class UpdateCustomerAddressUseCase {

    private final CustomerAddressGateway addressGateway;

    public UpdateCustomerAddressUseCase(CustomerAddressGateway addressGateway) {
        this.addressGateway = addressGateway;
    }

    public CustomerAddress execute(long customerId, long addressId, Place place) {
        CustomerAddress address = addressGateway.getByIdAndCustomer(addressId, customerId);
        address.update(place);
        return addressGateway.save(address);
    }
}
