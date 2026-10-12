package com.nailly.application.customer;

import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.shared.Place;

/** A cliente cadastra um endereço para atendimento a domicílio. */
public class AddCustomerAddressUseCase {

    private final CustomerGateway customerGateway;
    private final CustomerAddressGateway addressGateway;

    public AddCustomerAddressUseCase(CustomerGateway customerGateway, CustomerAddressGateway addressGateway) {
        this.customerGateway = customerGateway;
        this.addressGateway = addressGateway;
    }

    public CustomerAddress execute(long customerId, Place place) {
        customerGateway.getById(customerId);
        return addressGateway.save(new CustomerAddress(null, customerId, place));
    }
}
