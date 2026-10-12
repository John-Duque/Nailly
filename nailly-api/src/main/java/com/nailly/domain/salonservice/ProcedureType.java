package com.nailly.domain.salonservice;

import com.nailly.domain.shared.DomainValidation;

/** Técnica do serviço (ex.: esmaltação em gel, blindagem). */
public class ProcedureType {

    private final Long id;
    private final String name;
    private final boolean active;

    public ProcedureType(Long id, String name, boolean active) {
        this.id = id;
        this.name = DomainValidation.requireText(name, "Informe o nome da técnica.");
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
