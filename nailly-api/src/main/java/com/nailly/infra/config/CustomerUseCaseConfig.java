package com.nailly.infra.config;

import com.nailly.application.customer.AddCustomerAddressUseCase;
import com.nailly.application.customer.DeactivateCustomerAddressUseCase;
import com.nailly.application.customer.GetCustomerUseCase;
import com.nailly.application.customer.ListCustomerAddressesUseCase;
import com.nailly.application.customer.RegisterCustomerUseCase;
import com.nailly.application.customer.UpdateCustomerAddressUseCase;
import com.nailly.application.customer.UpdateCustomerProfileUseCase;
import com.nailly.application.user.AccountRegistration;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.customer.CustomerGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso de cliente como beans. */
@Configuration(proxyBeanMethods = false)
public class CustomerUseCaseConfig {

    @Bean
    public RegisterCustomerUseCase registerCustomerUseCase(AccountRegistration accountRegistration,
                                                           CustomerGateway customerGateway) {
        return new RegisterCustomerUseCase(accountRegistration, customerGateway);
    }

    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerGateway customerGateway) {
        return new GetCustomerUseCase(customerGateway);
    }

    @Bean
    public UpdateCustomerProfileUseCase updateCustomerProfileUseCase(CustomerGateway customerGateway) {
        return new UpdateCustomerProfileUseCase(customerGateway);
    }

    @Bean
    public AddCustomerAddressUseCase addCustomerAddressUseCase(CustomerGateway customerGateway,
                                                               CustomerAddressGateway addressGateway) {
        return new AddCustomerAddressUseCase(customerGateway, addressGateway);
    }

    @Bean
    public UpdateCustomerAddressUseCase updateCustomerAddressUseCase(CustomerAddressGateway addressGateway) {
        return new UpdateCustomerAddressUseCase(addressGateway);
    }

    @Bean
    public DeactivateCustomerAddressUseCase deactivateCustomerAddressUseCase(CustomerAddressGateway addressGateway) {
        return new DeactivateCustomerAddressUseCase(addressGateway);
    }

    @Bean
    public ListCustomerAddressesUseCase listCustomerAddressesUseCase(CustomerAddressGateway addressGateway) {
        return new ListCustomerAddressesUseCase(addressGateway);
    }
}
