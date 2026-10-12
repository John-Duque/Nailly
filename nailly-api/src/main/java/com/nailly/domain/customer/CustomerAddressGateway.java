package com.nailly.domain.customer;

import com.nailly.domain.shared.NotFoundException;

import java.util.List;
import java.util.Optional;

/** Acesso aos endereços das clientes. Implementado na camada infra. */
public interface CustomerAddressGateway {

    Optional<CustomerAddress> findById(long id);

    /** Endereço da cliente informada; endereço de outra pessoa também responde "não encontrado". */
    default CustomerAddress getByIdAndCustomer(long id, long customerId) {
        return findById(id)
                .filter(address -> address.belongsTo(customerId))
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado."));
    }

    List<CustomerAddress> findActiveByCustomer(long customerId);

    CustomerAddress save(CustomerAddress address);
}
