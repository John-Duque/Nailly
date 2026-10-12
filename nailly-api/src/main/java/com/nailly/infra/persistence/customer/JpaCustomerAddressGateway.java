package com.nailly.infra.persistence.customer;

import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.customer.CustomerAddressGateway;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.infra.persistence.shared.AddressColumns;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class JpaCustomerAddressGateway implements CustomerAddressGateway {

    private final CustomerAddressJpaRepository repository;

    public JpaCustomerAddressGateway(CustomerAddressJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerAddress> findById(long id) {
        return repository.findById(id).map(JpaCustomerAddressGateway::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> findActiveByCustomer(long customerId) {
        return repository.findByCustomerIdAndActiveTrueOrderByLabel(customerId).stream()
                .map(JpaCustomerAddressGateway::toDomain)
                .toList();
    }

    @Override
    public CustomerAddress save(CustomerAddress address) {
        CustomerAddressEntity entity = address.getId() == null
                ? new CustomerAddressEntity(address.getCustomerId())
                : repository.findById(address.getId())
                        .orElseThrow(() -> new NotFoundException("Endereço não encontrado."));
        entity.setLabel(address.getPlace().getName());
        entity.setAddress(AddressColumns.from(address.getPlace()));
        entity.setActive(address.isActive());
        return toDomain(repository.save(entity));
    }

    private static CustomerAddress toDomain(CustomerAddressEntity entity) {
        CustomerAddress address = new CustomerAddress(entity.getId(), entity.getCustomerId(),
                entity.getAddress().toPlace(entity.getLabel()));
        if (!entity.isActive()) {
            address.deactivate();
        }
        return address;
    }
}
