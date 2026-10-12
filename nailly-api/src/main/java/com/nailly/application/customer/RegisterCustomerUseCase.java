package com.nailly.application.customer;

import com.nailly.application.user.AccountRegistration;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;

/** Cadastro da cliente pelo app: cria a conta de acesso e o perfil. */
public class RegisterCustomerUseCase {

    private final AccountRegistration accountRegistration;
    private final CustomerGateway customerGateway;

    public RegisterCustomerUseCase(AccountRegistration accountRegistration, CustomerGateway customerGateway) {
        this.accountRegistration = accountRegistration;
        this.customerGateway = customerGateway;
    }

    public AuthenticatedAccount execute(String email, String password, String name, String phone) {
        User user = accountRegistration.create(email, password, Role.CUSTOMER);
        Customer customer = customerGateway.save(new Customer(null, user.getId(), name, phone));
        return new AuthenticatedAccount(user.getId(), Role.CUSTOMER, null, customer.getId());
    }
}
