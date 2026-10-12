package com.nailly.infra.persistence.customer;

import com.nailly.infra.persistence.shared.AddressColumns;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha de customer_addresses. */
@Entity
@Table(name = "customer_addresses")
public class CustomerAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private long customerId;

    @Column(name = "label", nullable = false, length = 60)
    private String label;

    @Embedded
    private AddressColumns address;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected CustomerAddressEntity() {
    }

    public CustomerAddressEntity(long customerId) {
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AddressColumns getAddress() {
        return address;
    }

    public void setAddress(AddressColumns address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
