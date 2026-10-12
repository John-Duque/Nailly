package com.nailly.infra.persistence.professional;

import com.nailly.infra.persistence.shared.AddressColumns;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha de professional_locations. */
@Entity
@Table(name = "professional_locations")
public class ProfessionalLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professional_id", nullable = false, updatable = false)
    private long professionalId;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Embedded
    private AddressColumns address;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ProfessionalLocationEntity() {
    }

    public ProfessionalLocationEntity(long professionalId) {
        this.professionalId = professionalId;
    }

    public Long getId() {
        return id;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
