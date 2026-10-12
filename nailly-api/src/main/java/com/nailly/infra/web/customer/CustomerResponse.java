package com.nailly.infra.web.customer;

import com.nailly.domain.customer.Customer;

public class CustomerResponse {

    private final Long id;
    private final String name;
    private final String phone;

    private CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.phone = customer.getPhone();
    }

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
