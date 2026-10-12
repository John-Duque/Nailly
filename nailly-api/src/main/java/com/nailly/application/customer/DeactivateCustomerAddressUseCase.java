package com.nailly.application.customer;

import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;

/** A cliente remove um endereço. Fica desativado, porque agendamentos antigos apontam para ele. */
public class DeactivateCustomerAddressUseCase {

    private final CustomerAddressGateway addressGateway;

    public DeactivateCustomerAddressUseCase(CustomerAddressGateway addressGateway) {
        this.addressGateway = addressGateway;
    }

    public CustomerAddress execute(long customerId, long addressId) {
        CustomerAddress address = addressGateway.getByIdAndCustomer(addressId, customerId);
        address.deactivate();
        return addressGateway.save(address);
    }
}
