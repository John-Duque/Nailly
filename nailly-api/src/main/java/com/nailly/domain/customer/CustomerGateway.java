package com.nailly.domain.customer;

import com.nailly.domain.shared.NotFoundException;

import java.util.Optional;

/** Acesso às clientes. Implementado na camada infra. */
public interface CustomerGateway {

    Optional<Customer> findById(long id);

    default Customer getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Cliente não encontrada."));
    }

    Optional<Customer> findByUserId(long userId);

    Customer save(Customer customer);
}
