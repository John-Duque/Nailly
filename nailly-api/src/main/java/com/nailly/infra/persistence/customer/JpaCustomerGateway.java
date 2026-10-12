package com.nailly.infra.persistence.customer;

import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.shared.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class JpaCustomerGateway implements CustomerGateway {

    private final CustomerJpaRepository repository;

    public JpaCustomerGateway(CustomerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(long id) {
        return repository.findById(id).map(JpaCustomerGateway::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByUserId(long userId) {
        return repository.findByUserId(userId).map(JpaCustomerGateway::toDomain);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customer.getId() == null
                ? new CustomerEntity(customer.getUserId())
                : repository.findById(customer.getId())
                        .orElseThrow(() -> new NotFoundException("Cliente não encontrada."));
        entity.setName(customer.getName());
        entity.setPhone(customer.getPhone());
        return toDomain(repository.save(entity));
    }

    private static Customer toDomain(CustomerEntity entity) {
        return new Customer(entity.getId(), entity.getUserId(), entity.getName(), entity.getPhone());
    }
}
