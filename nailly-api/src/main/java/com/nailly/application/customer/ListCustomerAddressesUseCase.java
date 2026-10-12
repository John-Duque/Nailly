package com.nailly.application.customer;

import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;

import java.util.List;

public class ListCustomerAddressesUseCase {

    private final CustomerAddressGateway addressGateway;

    public ListCustomerAddressesUseCase(CustomerAddressGateway addressGateway) {
        this.addressGateway = addressGateway;
    }

    public List<CustomerAddress> execute(long customerId) {
        return addressGateway.findActiveByCustomer(customerId);
    }
}
