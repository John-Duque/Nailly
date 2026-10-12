package com.nailly.infra.persistence.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAddressJpaRepository extends JpaRepository<CustomerAddressEntity, Long> {

    List<CustomerAddressEntity> findByCustomerIdAndActiveTrueOrderByLabel(long customerId);
}
