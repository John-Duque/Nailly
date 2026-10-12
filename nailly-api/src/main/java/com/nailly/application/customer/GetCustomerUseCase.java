package com.nailly.application.customer;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;

public class GetCustomerUseCase {

    private final CustomerGateway customerGateway;

    public GetCustomerUseCase(CustomerGateway customerGateway) {
        this.customerGateway = customerGateway;
    }

    public Customer execute(long customerId) {
        return customerGateway.getById(customerId);
    }
}
