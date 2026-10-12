package com.nailly.infra.web.salonservice;

import com.nailly.domain.salonservice.ProcedureType;

/** Técnica devolvida pela API. */
public class ProcedureTypeResponse {

    private final Long id;
    private final String name;

    private ProcedureTypeResponse(ProcedureType procedureType) {
        this.id = procedureType.getId();
        this.name = procedureType.getName();
    }

    public static ProcedureTypeResponse from(ProcedureType procedureType) {
        return new ProcedureTypeResponse(procedureType);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
