package com.nailly.application.customer;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;

/** A cliente edita nome e telefone. */
public class UpdateCustomerProfileUseCase {

    private final CustomerGateway customerGateway;

    public UpdateCustomerProfileUseCase(CustomerGateway customerGateway) {
        this.customerGateway = customerGateway;
    }

    public Customer execute(long customerId, String name, String phone) {
        Customer customer = customerGateway.getById(customerId);
        customer.updateProfile(name, phone);
        return customerGateway.save(customer);
    }
}
